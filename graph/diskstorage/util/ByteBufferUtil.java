/*
 * GRAKN.AI - THE KNOWLEDGE GRAPH
 * Copyright (C) 2019 Grakn Labs Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.core.graph.diskstorage.util;

import grakn.core.graph.diskstorage.util.Hex;

import java.nio.ByteBuffer;

/**
 * Utility methods for dealing with ByteBuffers in concurrent access
 * environments, i.e. these methods only use static access to the buffer.
 */
public class ByteBufferUtil {

    /* ################
     * ByteBuffer Creation Helpers
     * ################
     */

    public static ByteBuffer zeroByteBuffer(int len) {
        ByteBuffer res = ByteBuffer.allocate(len);
        for (int i = 0; i < len; i++) res.put((byte) 0);
        res.flip();
        return res;
    }

    public static ByteBuffer oneByteBuffer(int len) {
        ByteBuffer res = ByteBuffer.allocate(len);
        for (int i = 0; i < len; i++) res.put((byte) -1);
        res.flip();
        return res;
    }

    /* ################
     * ByteBuffer Comparison, HashCode and toString
     * ################
     */

    /**
     * Compares two {@link ByteBuffer}s and checks whether the first ByteBuffer is smaller than the second.
     *
     * @param a First ByteBuffer
     * @param b Second ByteBuffer
     * @return true if the first ByteBuffer is smaller than the second
     */
    public static boolean isSmallerThan(ByteBuffer a, ByteBuffer b) {
        return compare(a, b) < 0;
    }

    /**
     * Compares two {@link ByteBuffer}s and checks whether the first ByteBuffer is smaller than or equal to the second.
     *
     * @param a First ByteBuffer
     * @param b Second ByteBuffer
     * @return true if the first ByteBuffer is smaller than or equal to the second
     */
    public static boolean isSmallerOrEqualThan(ByteBuffer a, ByteBuffer b) {
        return compare(a, b) <= 0;
    }

    /**
     * Compares two {@link ByteBuffer}s according to their byte order (and not the byte value).
     * <p>
     *
     * @param b1 First ByteBuffer
     * @param b2 Second ByteBuffer
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    public static int compare(ByteBuffer b1, ByteBuffer b2) {
        if (b1 == b2) {
            return 0;
        }
        int p1 = b1.position(), p2 = b2.position();
        while (p1 < b1.limit() || p2 < b2.limit()) {
            if (p1 >= b1.limit()) {
                return -1;
            } else if (p2 >= b2.limit()) {
                return 1;
            } else {
                int cmp = compare(b1.get(p1), b2.get(p2));
                if (cmp != 0) {
                    return cmp;
                }
            }
            p1++;
            p2++;
        }
        return 0; //Must be equal
    }

    private static int compare(byte c1, byte c2) {
        int a = c1 & 0xff;
        int b = c2 & 0xff;

        return a - b;
    }

    public static String bytesToHex(ByteBuffer bytes) {
        final int offset = bytes.position();
        final int size = bytes.remaining();
        final char[] c = new char[size * 2];
        for (int i = 0; i < size; i++) {
            final int byteAsInteger = bytes.get(i + offset);
            c[i * 2] = Hex.byteToChar[(byteAsInteger & 0xf0) >> 4];
            c[1 + i * 2] = Hex.byteToChar[byteAsInteger & 0x0f];
        }
        return Hex.wrapCharArray(c);
    }


    public static String toString(ByteBuffer b, String separator) {
        StringBuilder s = new StringBuilder();
        for (int i = b.position(); i < b.limit(); i++) {
            if (i > b.position()) s.append(separator);
            byte c = b.get(i);
            if (c >= 0) s.append(c);
            else s.append(256 + c);
        }
        return s.toString();
    }
}
