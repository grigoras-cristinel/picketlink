/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.saml.v2.util;


import static org.picketlink.common.util.StaxUtil.writeCharacters;
import static org.picketlink.common.util.StaxUtil.writeStartElement;
import static org.picketlink.common.util.StaxUtil.writeEndElement;

import javax.xml.stream.XMLStreamWriter;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.xmlsec.w3.xmldsig.DSAKeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.RSAKeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509CertificateType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509DataType;
import org.w3c.dom.Element;

/**
 * Utility methods for stax writing
 * @author anil saldhana
 * @since Jan 28, 2013
 */
public class StaxWriterUtil {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    /**
     * Write the {@link KeyInfoType}
     * @param writer
     * @param keyInfo
     * @throws ProcessingException
     */
    public static void writeKeyInfo(XMLStreamWriter writer, KeyInfoType keyInfo) throws ProcessingException {
        if (keyInfo.getContent() == null || keyInfo.getContent().size() == 0)
            throw logger.writerInvalidKeyInfoNullContentError();
        StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.KEYINFO,
                WSTrustConstants.XMLDSig.DSIG_NS);
        StaxUtil.writeNameSpace(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.DSIG_NS);
        // write the keyInfo content.
        Object content = keyInfo.getContent().get(0);
        if (content instanceof Element) {
            Element element = (Element) keyInfo.getContent().get(0);
            StaxUtil.writeDOMNode(writer, element);
        } else if (content instanceof X509DataType) {
            X509DataType type = (X509DataType) content;
            if (type.getDataObjects().size() == 0)
                throw logger.writerNullValueError("X509Data");
            StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.X509DATA,
                    WSTrustConstants.XMLDSig.DSIG_NS);
            Object obj = type.getDataObjects().get(0);
            if (obj instanceof Element) {
                Element element = (Element) obj;
                StaxUtil.writeDOMElement(writer, element);
            } else if (obj instanceof X509CertificateType) {
                X509CertificateType cert = (X509CertificateType) obj;
                StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.X509CERT,
                        WSTrustConstants.XMLDSig.DSIG_NS);
                StaxUtil.writeCharacters(writer, new String(cert.getEncodedCertificate()));
                StaxUtil.writeEndElement(writer);
            }
            StaxUtil.writeEndElement(writer);
        } else if( content instanceof KeyValueType){
            KeyValueType keyvalueType = (KeyValueType) content;
            StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.KEYVALUE,
                    WSTrustConstants.XMLDSig.DSIG_NS);
            if(keyvalueType instanceof DSAKeyValueType){
                writeDSAKeyValueType(writer, (DSAKeyValueType) keyvalueType);
            }
            if(keyvalueType instanceof RSAKeyValueType){
                writeRSAKeyValueType(writer, (RSAKeyValueType) keyvalueType);
            }
            StaxUtil.writeEndElement(writer);
        } else
            throw new ProcessingException(ErrorCodes.UNSUPPORTED_TYPE + content);

        StaxUtil.writeEndElement(writer);
    }
    
    public static void writeRSAKeyValueType(XMLStreamWriter writer, RSAKeyValueType type) throws ProcessingException {
        String prefix = WSTrustConstants.XMLDSig.DSIG_PREFIX;
        
        StaxUtil.writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.RSA_KEYVALUE, WSTrustConstants.DSIG_NS);
        // write the rsa key modulus.
        byte[] modulus = type.getModulus();
        writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.MODULUS, WSTrustConstants.DSIG_NS);
        writeCharacters(writer, new String(modulus));
        writeEndElement(writer);

        // write the rsa key exponent.
        byte[] exponent = type.getExponent();
        writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.EXPONENT, WSTrustConstants.DSIG_NS);
        writeCharacters(writer, new String(exponent));
        writeEndElement(writer);

        writeEndElement(writer);
    }
    
    public static void writeDSAKeyValueType(XMLStreamWriter writer, DSAKeyValueType type) throws ProcessingException {
        
        String prefix = WSTrustConstants.XMLDSig.DSIG_PREFIX;
        
        writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.DSA_KEYVALUE, WSTrustConstants.DSIG_NS);
        
        byte[] p = type.getP();
        if(p != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.P, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(p));
            writeEndElement(writer);    
        }
        byte[] q = type.getQ();
        if(q != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.Q, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(q));
            writeEndElement(writer);    
        }
        byte[] g = type.getG();
        if(g != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.G, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(g));
            writeEndElement(writer);    
        }
        byte[] y = type.getY();
        if(y != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.Y, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(y));
            writeEndElement(writer);    
        }
        byte[] seed = type.getSeed();
        if(seed != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.SEED, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(seed));
            writeEndElement(writer);    
        }
        byte[] pgen = type.getPgenCounter();
        if(pgen != null){
            writeStartElement(writer, prefix, WSTrustConstants.XMLDSig.PGEN_COUNTER, WSTrustConstants.DSIG_NS);
            writeCharacters(writer, new String(pgen));
            writeEndElement(writer);    
        }
        
        writeEndElement(writer);
    }
}