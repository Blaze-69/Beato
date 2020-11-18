package com.example.beato

import java.util.*

class DoubleComparator : Comparator<Sound?> {
    override fun compare(p0: Sound?, p1: Sound?): Int {
        if (p0?.offset!! < p1?.offset!!) return -1
        return if (p1.offset < p0.offset) 1 else 0
    }
}