/**
 * OpenCvType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.hupo.psi.mi.mif;

public class OpenCvType  implements java.io.Serializable {
    private org.hupo.psi.mi.mif.Names names;

    private org.hupo.psi.mi.mif.Xref xref;

    private org.hupo.psi.mi.mif.Attribute[] attributeList;

    public OpenCvType() {
    }

    public OpenCvType(
           org.hupo.psi.mi.mif.Names names,
           org.hupo.psi.mi.mif.Xref xref,
           org.hupo.psi.mi.mif.Attribute[] attributeList) {
           this.names = names;
           this.xref = xref;
           this.attributeList = attributeList;
    }


    /**
     * Gets the names value for this OpenCvType.
     * 
     * @return names
     */
    public org.hupo.psi.mi.mif.Names getNames() {
        return names;
    }


    /**
     * Sets the names value for this OpenCvType.
     * 
     * @param names
     */
    public void setNames(org.hupo.psi.mi.mif.Names names) {
        this.names = names;
    }


    /**
     * Gets the xref value for this OpenCvType.
     * 
     * @return xref
     */
    public org.hupo.psi.mi.mif.Xref getXref() {
        return xref;
    }


    /**
     * Sets the xref value for this OpenCvType.
     * 
     * @param xref
     */
    public void setXref(org.hupo.psi.mi.mif.Xref xref) {
        this.xref = xref;
    }


    /**
     * Gets the attributeList value for this OpenCvType.
     * 
     * @return attributeList
     */
    public org.hupo.psi.mi.mif.Attribute[] getAttributeList() {
        return attributeList;
    }


    /**
     * Sets the attributeList value for this OpenCvType.
     * 
     * @param attributeList
     */
    public void setAttributeList(org.hupo.psi.mi.mif.Attribute[] attributeList) {
        this.attributeList = attributeList;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof OpenCvType)) return false;
        OpenCvType other = (OpenCvType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.names==null && other.getNames()==null) || 
             (this.names!=null &&
              this.names.equals(other.getNames()))) &&
            ((this.xref==null && other.getXref()==null) || 
             (this.xref!=null &&
              this.xref.equals(other.getXref()))) &&
            ((this.attributeList==null && other.getAttributeList()==null) || 
             (this.attributeList!=null &&
              java.util.Arrays.equals(this.attributeList, other.getAttributeList())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getNames() != null) {
            _hashCode += getNames().hashCode();
        }
        if (getXref() != null) {
            _hashCode += getXref().hashCode();
        }
        if (getAttributeList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAttributeList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAttributeList(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(OpenCvType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://psi.hupo.org/mi/mif", "openCvType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("names");
        elemField.setXmlName(new javax.xml.namespace.QName("http://psi.hupo.org/mi/mif", "names"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://psi.hupo.org/mi/mif", "names"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("xref");
        elemField.setXmlName(new javax.xml.namespace.QName("http://psi.hupo.org/mi/mif", "xref"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://psi.hupo.org/mi/mif", "xref"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attributeList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://psi.hupo.org/mi/mif", "attributeList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://psi.hupo.org/mi/mif", "attribute"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://psi.hupo.org/mi/mif", "attribute"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
