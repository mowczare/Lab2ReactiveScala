package utils

/**
  * Created by neo on 22.10.16.
  */
object StringUtils {
  def makeActorName(sentence: String): String = {
    sentence.split(' ').map(s => s.charAt(0).toUpper+s.substring(1)).mkString
  }
}
