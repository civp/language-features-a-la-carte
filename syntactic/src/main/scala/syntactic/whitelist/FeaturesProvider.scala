package syntactic.whitelist

import java.lang.annotation.AnnotationTypeMismatchException
import scala.annotation.ClassfileAnnotation

trait FeaturesProvider {

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

class ScalaFeature() extends ClassfileAnnotation
