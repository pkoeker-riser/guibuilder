CREATE TABLE Connection (
       ConnectionName VARCHAR(50) NOT NULL
     , JDBCDriver VARCHAR(128) NOT NULL
     , URL VARCHAR(128) NOT NULL
     , Schema VARCHAR(128)
     , Username VARCHAR(50)
     , Password VARCHAR(50)
     , SeqGet VARCHAR(128)
     , SeqSet VARCHAR(128)
     , OptimisticLockingField VARCHAR(32)
     , CreateUserField VARCHAR(32)
     , UpdateUserField VARCHAR(32)
     , IsolationLevel VARCHAR(32)
     , Version INTEGER
     , UserCreate VARCHAR(30)
     , UserUpdate VARCHAR(30)
     , PRIMARY KEY (ConnectionName)
);

