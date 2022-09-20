package org.session.app.utils

object OptionUtils {

  implicit class OptionComparatorExt[T](val left: Option[T]) extends AnyVal {
    def <(right: T)(implicit ord: Ordering[T]): Boolean = left.exists(l => ord.lt(l, right))

    def <=(right: T)(implicit ord: Ordering[T]): Boolean = left.exists(l => ord.lteq(l, right))

    def >(right: T)(implicit ord: Ordering[T]): Boolean = left.exists(l => ord.gt(l, right))

    def >=(right: T)(implicit ord: Ordering[T]): Boolean = left.exists(l => ord.gteq(l, right))
  }

}
