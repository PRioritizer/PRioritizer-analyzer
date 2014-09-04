package ghtorrent

import com.mongodb._
import com.mongodb.casbah.commons.MongoDBObject

class GHTorrentMongoDb(host: String, port: Int, username: String, password: String, databaseName: String) {
  private var client: MongoClient = _
  private var database: DB = _
  private var connected: Boolean = _

  def isOpen: Boolean = connected

  def open(): GHTorrentMongoDb = {
    if (connected)
      return this

    val server = new ServerAddress(host, port)

    val options = new MongoClientOptions.Builder()
      .connectTimeout(30000)
      .socketTimeout(30000)
      .readPreference(ReadPreference.secondaryPreferred())
      .build()

    client = if (username != null && username.nonEmpty) {
      val credential = MongoCredential.createMongoCRCredential(username, databaseName, password.toCharArray)
      new MongoClient(server, java.util.Arrays.asList(credential), options)
    } else {
      new MongoClient(server, options)
    }

    database = client.getDB(databaseName)
    connected = true

    this
  }

  def getByKey(collectionName: String, key: List[(String, Any)], select: List[String]) : Map[String, Any] = {
    if (key.exists { case (k, v) => k == null || k == "" })
      return Map()

    val query = MongoDBObject(key)

    val fields = new BasicDBObject()
    select.foreach(f => fields.put(f, 1))

    val collection = database.getCollection(collectionName)
    val result = collection.findOne(query, fields)
    select
      .map(f => getField[Any](result, f).map(v => (f, v)))
      .flatten
      .toMap
  }

  private def getField[T](obj: DBObject, fullPath: String): Option[T] = {
    def iteration(x: Any, path: Array[String]): Option[T] = {
      x match {
        case l: BasicDBList => Some(l.toArray.toList.map(e => iteration(e, path)).asInstanceOf[T])
        case o: DBObject => iteration(o.get(path.head), path.tail)
        case s: String => Some(s.asInstanceOf[T])
        case i: Int => Some(i.asInstanceOf[T])
        case _ => None
      }
    }
    iteration(obj, fullPath.split("""\."""))
  }

  def close(): Unit = {
    connected = false
    if (client != null)
      client.close()
  }
}
