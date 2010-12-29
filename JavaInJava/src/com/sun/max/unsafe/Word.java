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
package com.sun.max.unsafe;

import static com.sun.cri.bytecode.Bytecodes.*;
import static com.sun.max.platform.Platform.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import com.sun.cri.bytecode.*;
import com.sun.max.*;
import com.sun.max.annotate.*;
import com.sun.max.lang.*;
import com.sun.max.program.*;
import com.sun.max.vm.*;
import com.sun.max.vm.compiler.builtin.*;
import com.sun.max.vm.hosted.*;
import com.sun.max.vm.jni.*;
import com.sun.max.vm.value.*;

/**
 * A machine-word sized unboxed type. The {@code Word} type itself is mostly opaque, providing operations
 * to determine the {@linkplain #size() size} (in bytes) and {@linkplain #width() width} (in bits) of a word.
 * Subclasses define extra operations such as {@linkplain Offset signed} and {@linkplain Address unsigned}
 * arithmetic, and {@linkplain Pointer pointer} operations.
 *
 * In a {@linkplain MaxineVM#isHosted() hosted} runtime, {@code Word} type values are implemented with
 * {@linkplain Boxed boxed} values.
 *
 * The closure of {@code Word} types (i.e. all the classes that subclass {@link Word}) is {@linkplain #getSubclasses() discovered}
 * during initialization in a hosted environment. This discovery mechanism relies on the same package based
 * facility used to configure the schemes of a VM. Each package that defines one or more {@code Word} subclasses
 * must also declare a subclass of {@link MaxPackage} named "Package" that overrides {@link MaxPackage#wordSubclasses()}.
 *
 * @see WordValue
 *
 * @author Bernd Mathiske
 * @author Doug Simon
 */
public abstract class Word {

    protected Word() {
    }

    @INLINE
    public static Word zero() {
        return Address.zero();
    }

    @INLINE
    public static Word allOnes() {
        return Address.max();
    }

    @FOLD
    public static Endianness endianness() {
        return platform().endianness();
    }

    @FOLD
    public static WordWidth widthValue() {
        return platform().wordWidth();
    }

    @FOLD
    public static int width() {
        return widthValue().numberOfBits;
    }

    @FOLD
    public static int size() {
        return widthValue().numberOfBytes;
    }

    public final JniHandle asJniHandle() {
        if (this instanceof BoxedJniHandle) {
            return (BoxedJniHandle) this;
        }
        final Boxed box = (Boxed) this;
        return BoxedJniHandle.from(box.value());
    }

    public final Address asAddress() {
        if (this instanceof BoxedAddress) {
            return (BoxedAddress) this;
        }
        final Boxed box = (Boxed) this;
        return BoxedAddress.from(box.value());
    }

    public final Offset asOffset() {
        if (this instanceof BoxedOffset) {
            return (BoxedOffset) this;
        }
        final Boxed box = (Boxed) this;
        return BoxedOffset.from(box.value());
    }

    public final Size asSize() {
        if (this instanceof BoxedSize) {
            return (BoxedSize) this;
        }
        final Boxed box = (Boxed) this;
        return BoxedSize.from(box.value());
    }

    public final Pointer asPointer() {
        if (this instanceof BoxedPointer) {
            return (BoxedPointer) this;
        }
        final Boxed box = (Boxed) this;
        return BoxedPointer.from(box.value());
    }

    /**
     * @return bit index of the least significant bit set, or -1 if zero.
     */
    public final int leastSignificantBitSet() {
        return SpecialBuiltin.leastSignificantBit(this);
    }

    /**
     * @return bit index of the least significant bit set, or -1 if zero.
     */
    public final int mostSignificantBitSet() {
        return SpecialBuiltin.mostSignificantBit(this);
    }

    public final String toHexString() {
        String result = Long.toHexString(asAddress().toLong());
        if (width() == 32 && result.length() > 8) {
            result = result.substring(result.length() - 8);
        }
        return result;
    }

    public final String toPaddedHexString(char pad) {
        if (Word.width() == 64) {
            return Longs.toPaddedHexString(asAddress().toLong(), pad);
        }
        return Ints.toPaddedHexString(asAddress().toInt(), pad);
    }

    @Override
    public String toString() {
        return "$" + toHexString();
    }

    @Override
    public final int hashCode() {
        return asOffset().toInt();
    }

    @INLINE
    public final boolean isZero() {
        return equals(Word.zero());
    }

    @INLINE
    public final boolean isAllOnes() {
        return equals(Word.allOnes());
    }

    @INLINE
    public final boolean equals(Word other) {
        if (Word.width() == 64) {
            return asOffset().toLong() == other.asOffset().toLong();
        }
        return asOffset().toInt() == other.asOffset().toInt();
    }

    @Override
    public final boolean equals(Object other) {
        throw ProgramError.unexpected("must not call equals(Object) with Word argument");
    }

    /**
     * Reads an address from a given data input stream.
     */
    public static Word read(DataInput stream) throws IOException {
        if (width() == 64) {
            return Address.fromLong(stream.readLong());
        }
        return Address.fromInt(stream.readInt());
    }

    /**
     * Writes this address to a given data output stream.
     */
    @INLINE
    public final void write(DataOutput stream) throws IOException {
        if (width() == 64) {
            stream.writeLong(asAddress().toLong());
        } else {
            stream.writeInt(asAddress().toInt());
        }
    }

    /**
     * Reads an address from a given input stream using a given endianness.
     */
    public static Word read(InputStream inputStream, Endianness endianness) throws IOException {
        if (width() == 64) {
            return Address.fromLong(endianness.readLong(inputStream));
        }
        return Address.fromInt(endianness.readInt(inputStream));
    }

    /**
     * Writes this address to a given output stream using a given endianness.
     */
    @INLINE
    public final void write(OutputStream outputStream, Endianness endianness) throws IOException {
        if (width() == 64) {
            endianness.writeLong(outputStream, asAddress().toLong());
        } else {
            endianness.writeInt(outputStream, asAddress().toInt());
        }
    }
}
