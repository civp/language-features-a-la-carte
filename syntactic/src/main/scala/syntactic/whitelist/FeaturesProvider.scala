package syntactic.whitelist

/**
 * When implemented by an object that defines Features, automatically provides a list of all the features
 * defined in the object (i.e. all public objects that extend the [[Feature]] trait)
 */
trait FeaturesProvider {

  /**
   * all the features defined in this (i.e. all public objects that extend the [[Feature]] trait)
   */
  val allDefinedFeatures: List[Feature] = {
    val clazz = getClass
    val mirror = scala.reflect.runtime.universe.runtimeMirror(clazz.getClassLoader)
    val modulesWithUncheckedType = mirror
      .classSymbol(clazz)
      .info
      .members
      .filter(_.isPublic)
      .filter(_.isModule)
      .filter(_.asModule.typeSignature <:< mirror.typeOf[Feature])
      .map(member => mirror.reflectModule(member.asModule).instance)

    assert(modulesWithUncheckedType.forall(_.isInstanceOf[Feature]))
    modulesWithUncheckedType.map(_.asInstanceOf[Feature]).toList
  }

}
