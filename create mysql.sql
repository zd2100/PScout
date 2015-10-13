DROP Table IF EXISTS Classes;

CREATE TABLE Classes
(
ID int auto_increment PRIMARY KEY,
ClassName varchar(255) NOT NULL,
Version varchar(15) NOT NULL,
Access varchar(63),
SuperClass varchar(255),
Signature text,
Interfaces text,
IsAbstract bit,
IsInterface bit,
IsEnum bit
-- CONSTRAINT PK_Class PRIMARY KEY CLUSTERED (ClassName, Version)
);

CREATE UNIQUE INDEX IDX_Classes_ClassName_Version
ON Classes (ClassName, Version);

CREATE INDEX IDX_Classes_ClassName
ON Classes (ClassName);

CREATE INDEX IDX_Classes_Version
ON Classes (Version);

CREATE INDEX IDX_Classes_SuperClass
ON Classes (SuperClass);

CREATE FULLTEXT INDEX  IDX_Classes_Signature
ON Classes (Signature);

CREATE FULLTEXT INDEX IDX_Classes_Interfaces
ON Classes (Interfaces);

CREATE INDEX IDX_Classes_IsInterface
ON Classes (IsInterface);






DROP TABLE IF EXISTS Methods;

CREATE TABLE Methods
(
ID int auto_increment PRIMARY KEY,
ClassName varchar(255) NOT NULL,
MethodName varchar(127) NOT NULL,
Version varchar(15) NOT NULL,
Access varchar(63),
Signature text,
Descriptor text NOT NULL,
Exceptions text,
IsAbstract bit,
IsNative bit
-- CONSTRAINT PK_Method PRIMARY KEY CLUSTERED (ClassName, MethodName, Version)
);


CREATE INDEX IDX_Methods_ClassName
ON Methods (ClassName);

CREATE INDEX IDX_Methods_MethodName
ON Methods (MethodName);

CREATE INDEX IDX_Methods_Version
ON Methods (Version);

CREATE FULLTEXT INDEX IDX_Methods_Signature
ON Methods (Signature);

CREATE FULLTEXT INDEX IDX_Methods_Exceptions
ON Methods (Exceptions);

CREATE FULLTEXT INDEX IDX_Methods_Descriptor
ON Methods (Descriptor);

CREATE INDEX IDX_METHODS_IsAbstract
ON Methods (IsAbstract);

CREATE INDEX IDX_Methods_IsNative
ON Methods (IsNative);




DROP TABLE IF EXISTS MethodInvocations;

CREATE TABLE MethodInvocations
(
ID int auto_increment PRIMARY KEY,
InvokeType varchar(15) NOT NULL,
CallingClass varchar(255) NOT NULL,
CallingMethod varchar(127) NOT NULL,
CallingMethodDescriptor text NOT NULL,
TargetClass varchar(255) NOT NULL,
TargetMethod varchar(127) NOT NULL,
TargetMethodDescriptor text NOT NULL,
Version varchar(15) NOT NULL
-- CONSTRAINT PK_MethodInvocations PRIMARY KEY CLUSTERED (CallingClass, CallingMethod, TargetClass,TargetMethod, Version)
);

CREATE INDEX IDX_MethodInvocations_CallingClass
ON MethodInvocations (CallingClass);

CREATE INDEX IDX_MethodInvocations_CallingMethod
ON MethodInvocations (CallingMethod);

CREATE INDEX IDX_MethodInvocations_TargetClass
ON MethodInvocations (TargetClass);

CREATE INDEX IDX_MethodInvocations_TargetMethod
ON MethodInvocations (TargetMethod);

CREATE INDEX IDX_MethodInvocations_InvokeType
ON MethodInvocations (InvokeType);


DROP TABLE IF EXISTS Permissions;
CREATE TABLE Permissions
(
	Permission varchar(511) NOT NULL,
	Version varchar(31) NOT NULL
	-- CONSTRAINT PK_Permissions PRIMARY KEY CLUSTERED (Permission, Version)
);