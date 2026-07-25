package com.example.sdui.app

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun getNowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
