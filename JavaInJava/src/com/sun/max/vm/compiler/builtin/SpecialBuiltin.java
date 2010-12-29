/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation, these intellectual property
 * rights may include one or more of the U.S. patents listed at http://www.sun.com/patents and one or
 * more additional patents or pending patent applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun
 * Microsystems, Inc. standard license agreement and applicable provisions of the FAR and its
 * supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks
 * are used under license and are trademarks or registered trademarks of SPARC International, Inc. in the
 * U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open
 * Company, Ltd.
 */
package com.sun.max.vm.compiler.builtin;

import com.sun.max.unsafe.Word;

/**
 * @author Bernd Mathiske
 */
public abstract class SpecialBuiltin {

    /**
     * Returns the index of the least significant bit set in a given value.
     *
     * @param value the value to scan for the least significant bit
     * @return the index of the least significant bit within {@code value} or {@code -1} if {@code value == 0}
     */
    public static int leastSignificantBit(Word value) {
        long l = value.asAddress().toLong();
        if (l == 0) {
            return -1;
        }
        return Long.numberOfTrailingZeros(l);
    }

    /**
     * Returns the index to the most significant bit set in a given value.
     *
     * @param value the value to scan for the most significant bit
     * @return the index to the most significant bit within {@code value} or {@code -1} if {@code value == 0}
     */
    public static int mostSignificantBit(Word value) {
        long l = value.asAddress().toLong();
        if (l == 0) {
            return -1;
        }
        return Long.numberOfTrailingZeros(l);
    }
}
