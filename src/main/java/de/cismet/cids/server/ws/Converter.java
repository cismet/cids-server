/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.ws;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Helper class for various convertion related tasks like (de)serialisation.
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class Converter {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Converter object.
     */
    private Converter() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Serialises an object into a <code>byte[]</code> using an {@link ObjectOutputStream}.
     *
     * @param   o  the object to serialise
     *
     * @return  the serialised object in a <code>byte[]</code>
     *
     * @throws  IOException  if an error occurs during serialisation
     */
    public static byte[] serialise(final Object o) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.flush();

        final byte[] bytes = baos.toByteArray();
        oos.close();

        return bytes;
    }

    /**
     * Deserialises an object from a <code>byte[]</code> using an {@link ObjectInputStream}.
     *
     * @param   <T>    the type of the resulting <code>Object</code>
     * @param   bytes  the serialised <code>byte[]</code>
     * @param   type   the <code>Class</code> of the resulting <code>Object</code>
     *
     * @return  the deserialised object
     *
     * @throws  IOException             if an error occurs during deserialisation
     * @throws  ClassNotFoundException  if any of the classes of the object cannot be found
     */
    public static <T> T deserialise(final byte[] bytes, final Class<T> type) throws IOException,
        ClassNotFoundException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final Object o = ois.readObject();
        ois.close();

        return (T)o;
    }

    /**
     * Serialises the given <code>Object</code> to a base64 encoded <code>byte[]</code>.
     *
     * @param   o  the <code>Object</code> to serialise
     *
     * @return  a base64 encoded <code>byte[]</code>
     *
     * @throws  IOException  if any error occurs during serialisation
     *
     * @see     #toBase64(byte[])
     * @see     #serialise(java.lang.Object)
     */
    public static byte[] serialiseToBase64(final Object o) throws IOException {
        return toBase64(serialise(o));
    }

    /**
     * Deserialises the given base64 encoded <code>bate[]</code> to the desired <code>Object</code>.
     *
     * @param   <T>    the type of the resulting <code>Object</code>
     * @param   bytes  the serialised base64 encoded <code>byte[]</code>
     * @param   type   the <code>Class</code> of the resulting <code>Object</code>
     *
     * @return  the deserialised <code>Object</code>
     *
     * @throws  IOException             if an error occurs during deserialisation
     * @throws  ClassNotFoundException  if any of the classes of the object cannot be found
     *
     * @see     #deserialise(byte[], java.lang.Class)
     * @see     #fromBase64(byte[])
     */
    public static <T> T deserialiseFromBase64(final byte[] bytes, final Class<T> type) throws IOException,
        ClassNotFoundException {
        return deserialise(fromBase64(bytes), type);
    }

    /**
     * Encodes the given <code>byte[]</code> to base64.
     *
     * @param   bytes  bytes to encode
     *
     * @return  the base64 encoded bytes
     *
     * @see     Base64#encodeBase64(byte[])
     */
    public static byte[] toBase64(final byte[] bytes) {
        return Base64.encodeBase64(bytes);
    }

    /**
     * Decodes the given <code>byte[]</code> from base64.
     *
     * @param   bytes  bytes to decode
     *
     * @return  the system encoded bytes
     *
     * @see     Base64#decodeBase64(byte[])
     */
    public static byte[] fromBase64(final byte[] bytes) {
        return Base64.decodeBase64(bytes);
    }

    /**
     * Create a <code>String</code> from a given <code>byte[]</code>. The given bytes will be base64 encoded and the
     * resulting <code>String</code> will have ASCII encoding.
     *
     * @param   bytes  the bytes that shall be represented as <code>String</code>
     *
     * @return  an ASCII encoded <code>String</code>
     *
     * @throws  IllegalStateException  if the system does not support ASCII encoding
     *
     * @see     #toBase64(byte[])
     */
    public static String toString(final byte[] bytes) {
        try {
            return new String(toBase64(bytes), "ASCII");                     // NOI18N
        } catch (final UnsupportedEncodingException ex) {
            final String message = "system does not support ASCII encoding"; // NOI18N
            throw new IllegalStateException(message, ex);
        }
    }

    /**
     * Create a <code>byte[]</code> from the given <code>String</code>. The given String is supposed to be ASCII
     * encoded.
     *
     * @param   bytes  the <code>String</code> to be converted
     *
     * @return  an system encoded <code>byte[]</code>
     *
     * @throws  IllegalStateException  if the system does not support ASCII encoding
     *
     * @see     #fromBase64(byte[])
     */
    public static byte[] fromString(final String bytes) {
        try {
            return fromBase64(bytes.getBytes("ASCII"));                      // NOI18N
        } catch (final UnsupportedEncodingException ex) {
            final String message = "system does not support ASCII encoding"; // NOI18N
            throw new IllegalStateException(message, ex);
        }
    }

    /**
     * Creates an ASCII encoded <code>String</code> from an <code>Object</code>.
     *
     * @param   o  the object to serialise
     *
     * @return  the ASCII encoded <code>String</code> representation of an <code>Object</code>
     *
     * @throws  IOException  if any error occurs during serialisation
     *
     * @see     #toString(byte[])
     * @see     #serialise(java.lang.Object)
     */
    public static String serialiseToString(final Object o) throws IOException {
        return toString(serialise(o));
    }

    /**
     * Creates an <code>Object</code> from an ASCII encoded <code>String</code>.
     *
     * @param   <T>   the type of the <code>Object</code> to be created
     * @param   s     the ASCII encoded <code>String</code> representation of an <code>Object</code>
     * @param   type  the type class fo the <code>Object</code> to be created
     *
     * @return  the <code>Object</code> create from the ASCII encoded <code>String</code>
     *
     * @throws  IOException             if any error occurs during deserialisation
     * @throws  ClassNotFoundException  if the desired <code>Class</code> cannot be found
     *
     * @see     #deserialise(byte[], java.lang.Class)
     * @see     #fromString(java.lang.String)
     */
    public static <T> T deserialiseFromString(final String s, final Class<T> type) throws IOException,
        ClassNotFoundException {
        return deserialise(fromString(s), type);
    }
}
