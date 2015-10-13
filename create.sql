IF OBJECT_ID('Classes') IS NOT NULL
	DROP TABLE Classes;
GO

CREATE TABLE Classes
(
ID int Identity(1,1) PRIMARY KEY,
ClassName varchar(256) NOT NULL,
Version varchar(16) NOT NULL,
Access varchar(64),
SuperClass varchar(256),
Signature varchar(900),
Interfaces varchar(900),
IsAbstract bit,
IsInterface bit,
IsEnum bit,
-- CONSTRAINT PK_Class PRIMARY KEY CLUSTERED (ClassName, Version)
)
GO

CREATE UNIQUE INDEX IDX_Classes_ClassName_Version
ON Classes (ClassName, Version)

CREATE INDEX IDX_Classes_ClassName
ON Classes (ClassName)

CREATE INDEX IDX_Classes_Version
ON Classes (Version)

CREATE INDEX IDX_Classes_SuperClass
ON Classes (SuperClass)

CREATE INDEX IDX_Classes_Signature
ON Classes (Signature)

CREATE INDEX IDX_Classes_Interfaces
ON Classes (Interfaces)

CREATE INDEX IDX_Classes_IsInterface
ON Classes (IsInterface)








IF OBJECT_ID('Methods') IS NOT NULL
	DROP TABLE Methods;
GO


CREATE TABLE Methods
(
ID int identity(1,1) PRIMARY KEY,
ClassName varchar(256) NOT NULL,
MethodName varchar(128) NOT NULL,
Version varchar(16) NOT NULL,
Access varchar(64),
Signature varchar(900),
Descriptor varchar(max) NOT NULL,
Exceptions varchar(900),
IsAbstract bit,
IsNative bit,
-- CONSTRAINT PK_Method PRIMARY KEY CLUSTERED (ClassName, MethodName, Version)
)
GO

CREATE INDEX IDX_Methods_ClassName
ON Methods (ClassName)

CREATE INDEX IDX_Methods_MethodName
ON Methods (MethodName)

CREATE INDEX IDX_Methods_Version
ON Methods (Version)

CREATE INDEX IDX_Methods_Signature
ON Methods (Signature)

CREATE INDEX IDX_Methods_Exceptions
ON Methods (Exceptions)

CREATE INDEX IDX_METHODS_IsAbstract
ON Methods (IsAbstract)

CREATE INDEX IDX_Methods_IsNative
ON Methods (IsNative)






IF OBJECT_ID('MethodInvocations') IS NOT NULL
	DROP TABLE MethodInvocations
GO

CREATE TABLE MethodInvocations
(
ID int IDENTITY(1,1) PRIMARY KEY,
InvokeType varchar(16) NOT NULL,
CallingClass varchar(256) NOT NULL,
CallingMethod varchar(128) NOT NULL,
CallingMethodDescriptor varchar(max) NOT NULL,
TargetClass varchar(256) NOT NULL,
TargetMethod varchar(128) NOT NULL,
TargetMethodDescriptor varchar(max) NOT NULL,
Version varchar(16) NOT NULL
--CONSTRAINT PK_MethodInvocations PRIMARY KEY CLUSTERED (CallingClass, CallingMethod, TargetClass,TargetMethod, Version)
)
GO

CREATE INDEX IDX_MethodInvocations_CallingClass
ON MethodInvocations (CallingClass)

CREATE INDEX IDX_MethodInvocations_CallingMethod
ON MethodInvocations (CallingMethod)

CREATE INDEX IDX_MethodInvocations_TargetClass
ON MethodInvocations (TargetClass)

CREATE INDEX IDX_MethodInvocations_TargetMethod
ON MethodInvocations (TargetMethod)

CREATE INDEX IDX_MethodInvocations_InvokeType
ON MethodInvocations (InvokeType)


IF OBJECT_ID('Permissions') IS NOT NULL
	DROP TABLE Permissions;
GO

CREATE TABLE Permissions
(
	Permission varchar(512) NOT NULL,
	Version varchar(32) NOT NULL,
	FoundBy varchar(128),
	CONSTRAINT PK_Permissions PRIMARY KEY CLUSTERED (Permission, Version)
)



IF OBJECT_ID('KnownPermissionChecks') IS NOT NULL
	DROP TABLE KnownPermissionChecks;
GO
CREATE TABLE KnownPermissionChecks(
ID int IDENTITY(1,1) PRIMARY KEY,
ClassName varchar(256) NOT NULL,
MethodName varchar(128) NOT NULL,
Descriptor varchar(max) NOT NULL,
Version varchar(16) NOT NULL
)

