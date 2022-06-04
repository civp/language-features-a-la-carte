package syntactic.whitelist

import java.lang.annotation.AnnotationTypeMismatchException
import scala.annotation.ClassfileAnnotation

/**
 * When implemented by a class that defines Features, automatically provides a list of all the features
 * defined in the class that are annotated with @ScalaFeature
 */
trait FeaturesProvider {

  /**
   * All the features defined in this class that are annotated with @ScalaFeature
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
      // TODO either one or the other of the two lines (and if relying on type then change the exception in the PF below)
      .filter(_.annotations.exists(_.tree.tpe =:= mirror.typeOf[ScalaFeature]))
      //.filter(_.asModule.typeSignature <:< mirror.typeOf[Feature])
      .map(member => mirror.reflectModule(member.asModule).instance)
    val modules = modulesWithUncheckedType.map {
      case feature: Feature => feature
      case any => throw new AnnotationTypeMismatchException(null, any.getClass.getName)
    }
    modules.toList
  }

}

/**
 * Marks the Features that should be considered by the automatic building of a list of defined Features
 *
 * Only intended to be used on singleton objects extending the Feature interface
 */
class ScalaFeature() extends ClassfileAnnotation
