CREATE TEXT TABLE TableRequest (
       TableRequestId INTEGER NOT NULL
     , TableName VARCHAR(64) NOT NULL
     , Alias VARCHAR(64)
     , RequestType INT NOT NULL
     , RefName VARCHAR(64)
     , x_distinct BIT DEFAULT '0' NOT NULL
     , pk VARCHAR(128)
     , fk VARCHAR(128)
     , selfreference BIT DEFAULT '0' NOT NULL
     , x_where VARCHAR(256)
     , x_orderby VARCHAR(128)
     , x_groupby VARCHAR(128)
     , x_join VARCHAR(32)
     , readonly BIT DEFAULT '0' NOT NULL
     , transient BIT DEFAULT '0' NOT NULL
     , virtual VARCHAR(64)
     , suppress BIT DEFAULT '0' NOT NULL
     , fetchsize INTEGER
     , MaxFieldSize INTEGER
     , MaxRows INTEGER
     , QueryTimeout INTEGER
     , x_OnUpdate INTEGER DEFAULT '3'
     , x_OnDelete INTEGER DEFAULT '0'
     , FK_TableRequestId INTEGER
     , FK_RequestId INTEGER
     , PRIMARY KEY (TableRequestId)
     , CONSTRAINT FK_DataTable_1 FOREIGN KEY (FK_RequestId)
                  REFERENCES Request (RequestId)
     , CONSTRAINT FK_TableRequest_2 FOREIGN KEY (FK_TableRequestId)
                  REFERENCES TableRequest (TableRequestId)
);
CREATE INDEX IX_TableRequest_1 ON TableRequest (FK_TableRequestId);

