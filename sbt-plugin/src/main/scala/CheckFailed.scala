package sbtlanguagefeatures

import sbt.FeedbackProvidedException

private[sbtlanguagefeatures] final class CheckFailed(msg: String)
    extends RuntimeException(msg)
    with FeedbackProvidedException
