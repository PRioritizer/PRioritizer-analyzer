package git

trait Provider {
  def merger: MergeTester
  def info: InfoGetter
}
