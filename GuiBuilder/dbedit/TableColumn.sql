CREATE TEXT TABLE TableColumn (
       ColumnId INTEGER NOT NULL
     , ColumnName VARCHAR(64) NOT NULL
     , Alias VARCHAR(64)
     , readonly BIT DEFAULT '0' NOT NULL
     , DisplayOrder INTEGER
     , DataType INT
     , DisplayType VARCHAR(32)
     , display BIT DEFAULT '0' NOT NULL
     , edit BIT DEFAULT '0' NOT NULL
     , filter BIT DEFAULT '0' NOT NULL
     , overview BIT
     , sort INTEGER
     , lookupTable VARCHAR(64)
     , ValueMember VARCHAR(64)
     , DisplayMember VARCHAR(64)
     , PrimaryKey BIT DEFAULT '0' NOT NULL
     , KeySeq INTEGER
     , ForeignKey BIT DEFAULT '0' NOT NULL
     , NotNull BIT DEFAULT '0' NOT NULL
     , AutoId BIT DEFAULT '0' NOT NULL
     , Size INTEGER
     , DecimalDigits INTEGER
     , DefaultValue VARCHAR(64)
     , Transient BIT DEFAULT '0' NOT NULL
     , FK_TableRequestId INTEGER
     , PRIMARY KEY (ColumnId)
     , CONSTRAINT FK_TableColumn_1 FOREIGN KEY (FK_TableRequestId)
                  REFERENCES TableRequest (TableRequestId)
);

