/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.pacbio.common.models.pipelines;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class InBinding extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"InBinding\",\"namespace\":\"com.pacbio.common.models.pipelines\",\"fields\":[{\"name\":\"taskTypeId\",\"type\":\"string\",\"doc\":\"Task type (tool_contract_id)\"},{\"name\":\"index\",\"type\":\"int\",\"doc\":\"Positional index of Task Input\"},{\"name\":\"instanceId\",\"type\":\"int\",\"doc\":\"Task Type instance id. A pipeline can have multiple instances of the same task type.\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  /** Task type (tool_contract_id) */
  @Deprecated public java.lang.CharSequence taskTypeId;
  /** Positional index of Task Input */
  @Deprecated public int index;
  /** Task Type instance id. A pipeline can have multiple instances of the same task type. */
  @Deprecated public int instanceId;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public InBinding() {}

  /**
   * All-args constructor.
   */
  public InBinding(java.lang.CharSequence taskTypeId, java.lang.Integer index, java.lang.Integer instanceId) {
    this.taskTypeId = taskTypeId;
    this.index = index;
    this.instanceId = instanceId;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return taskTypeId;
    case 1: return index;
    case 2: return instanceId;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: taskTypeId = (java.lang.CharSequence)value$; break;
    case 1: index = (java.lang.Integer)value$; break;
    case 2: instanceId = (java.lang.Integer)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'taskTypeId' field.
   * Task type (tool_contract_id)   */
  public java.lang.CharSequence getTaskTypeId() {
    return taskTypeId;
  }

  /**
   * Sets the value of the 'taskTypeId' field.
   * Task type (tool_contract_id)   * @param value the value to set.
   */
  public void setTaskTypeId(java.lang.CharSequence value) {
    this.taskTypeId = value;
  }

  /**
   * Gets the value of the 'index' field.
   * Positional index of Task Input   */
  public java.lang.Integer getIndex() {
    return index;
  }

  /**
   * Sets the value of the 'index' field.
   * Positional index of Task Input   * @param value the value to set.
   */
  public void setIndex(java.lang.Integer value) {
    this.index = value;
  }

  /**
   * Gets the value of the 'instanceId' field.
   * Task Type instance id. A pipeline can have multiple instances of the same task type.   */
  public java.lang.Integer getInstanceId() {
    return instanceId;
  }

  /**
   * Sets the value of the 'instanceId' field.
   * Task Type instance id. A pipeline can have multiple instances of the same task type.   * @param value the value to set.
   */
  public void setInstanceId(java.lang.Integer value) {
    this.instanceId = value;
  }

  /** Creates a new InBinding RecordBuilder */
  public static com.pacbio.common.models.pipelines.InBinding.Builder newBuilder() {
    return new com.pacbio.common.models.pipelines.InBinding.Builder();
  }
  
  /** Creates a new InBinding RecordBuilder by copying an existing Builder */
  public static com.pacbio.common.models.pipelines.InBinding.Builder newBuilder(com.pacbio.common.models.pipelines.InBinding.Builder other) {
    return new com.pacbio.common.models.pipelines.InBinding.Builder(other);
  }
  
  /** Creates a new InBinding RecordBuilder by copying an existing InBinding instance */
  public static com.pacbio.common.models.pipelines.InBinding.Builder newBuilder(com.pacbio.common.models.pipelines.InBinding other) {
    return new com.pacbio.common.models.pipelines.InBinding.Builder(other);
  }
  
  /**
   * RecordBuilder for InBinding instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<InBinding>
    implements org.apache.avro.data.RecordBuilder<InBinding> {

    private java.lang.CharSequence taskTypeId;
    private int index;
    private int instanceId;

    /** Creates a new Builder */
    private Builder() {
      super(com.pacbio.common.models.pipelines.InBinding.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(com.pacbio.common.models.pipelines.InBinding.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.taskTypeId)) {
        this.taskTypeId = data().deepCopy(fields()[0].schema(), other.taskTypeId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.index)) {
        this.index = data().deepCopy(fields()[1].schema(), other.index);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.instanceId)) {
        this.instanceId = data().deepCopy(fields()[2].schema(), other.instanceId);
        fieldSetFlags()[2] = true;
      }
    }
    
    /** Creates a Builder by copying an existing InBinding instance */
    private Builder(com.pacbio.common.models.pipelines.InBinding other) {
            super(com.pacbio.common.models.pipelines.InBinding.SCHEMA$);
      if (isValidValue(fields()[0], other.taskTypeId)) {
        this.taskTypeId = data().deepCopy(fields()[0].schema(), other.taskTypeId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.index)) {
        this.index = data().deepCopy(fields()[1].schema(), other.index);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.instanceId)) {
        this.instanceId = data().deepCopy(fields()[2].schema(), other.instanceId);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'taskTypeId' field */
    public java.lang.CharSequence getTaskTypeId() {
      return taskTypeId;
    }
    
    /** Sets the value of the 'taskTypeId' field */
    public com.pacbio.common.models.pipelines.InBinding.Builder setTaskTypeId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.taskTypeId = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'taskTypeId' field has been set */
    public boolean hasTaskTypeId() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'taskTypeId' field */
    public com.pacbio.common.models.pipelines.InBinding.Builder clearTaskTypeId() {
      taskTypeId = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'index' field */
    public java.lang.Integer getIndex() {
      return index;
    }
    
    /** Sets the value of the 'index' field */
    public com.pacbio.common.models.pipelines.InBinding.Builder setIndex(int value) {
      validate(fields()[1], value);
      this.index = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'index' field has been set */
    public boolean hasIndex() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'index' field */
    public com.pacbio.common.models.pipelines.InBinding.Builder clearIndex() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'instanceId' field */
    public java.lang.Integer getInstanceId() {
      return instanceId;
    }
    
    /** Sets the value of the 'instanceId' field */
    public com.pacbio.common.models.pipelines.InBinding.Builder setInstanceId(int value) {
      validate(fields()[2], value);
      this.instanceId = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'instanceId' field has been set */
    public boolean hasInstanceId() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'instanceId' field */
    public com.pacbio.common.models.pipelines.InBinding.Builder clearInstanceId() {
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public InBinding build() {
      try {
        InBinding record = new InBinding();
        record.taskTypeId = fieldSetFlags()[0] ? this.taskTypeId : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.index = fieldSetFlags()[1] ? this.index : (java.lang.Integer) defaultValue(fields()[1]);
        record.instanceId = fieldSetFlags()[2] ? this.instanceId : (java.lang.Integer) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}