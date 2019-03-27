CREATE TEXT TABLE Request (
       RequestId INTEGER NOT NULL
     , DatasetName VARCHAR(64) NOT NULL
     , readonly BIT DEFAULT '0' NOT NULL
     , transient BIT DEFAULT '0' NOT NULL
     , DatabaseName VARCHAR(64)
     , ScriptDetail VARCHAR(128)
     , ScriptFilter VARCHAR(128)
     , ScriptOverview VARCHAR(128)
     , PRIMARY KEY (RequestId)
);

