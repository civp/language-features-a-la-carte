package translator

/**
 * To be thrown when an expected error occurs during translation
 *
 * Means that the method cannot be translated
 */
case class TranslatorException(msg: String) extends Exception(msg)

