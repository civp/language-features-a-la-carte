package features.sbtplugin

import sbt.FeedbackProvidedException

private[sbtplugin] final class CheckFailed(msg: String)
    extends RuntimeException(msg)
    with FeedbackProvidedException
