SET FOREIGN_KEY_CHECKS=0; 
DROP Table IF EXISTS Classes;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE Classes
(
ID int auto_increment PRIMARY KEY,
ClassName varchar(127) NOT NULL,
Version varchar(15) NOT NULL,
Access varchar(63),
SuperClass varchar(127),
Signature text,
Interfaces text,
IsAbstract bit,
IsInterface bit,
IsEnum bit,
IsPublic bit
);

CREATE UNIQUE INDEX IDX_Classes_ClassName_Version
ON Classes (ClassName, Version);

CREATE INDEX IDX_Classes_ClassName
ON Classes (ClassName);

CREATE INDEX IDX_Classes_Version
ON Classes (Version);

CREATE INDEX IDX_Classes_SuperClass
ON Classes (SuperClass);

CREATE INDEX IDX_Classes_Signature
ON Classes (Signature(255));

CREATE INDEX IDX_Classes_Interfaces
ON Classes (Interfaces(255));

CREATE INDEX IDX_Classes_IsInterface
ON Classes (IsInterface);

CREATE INDEX IDX_Classes_IsPublic
ON Classes (IsPublic);





SET FOREIGN_KEY_CHECKS=0; 
DROP TABLE IF EXISTS Methods;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE Methods
(
ID int auto_increment PRIMARY KEY,
ClassId int NOT NULL,
MethodName varchar(127) NOT NULL,
Version varchar(15) NOT NULL,
Access varchar(63),
Signature text,
Descriptor text NOT NULL,
Exceptions text,
IsAbstract bit,
IsNative bit,
IsPublic bit,
FOREIGN KEY (ClassId) REFERENCES Classes(ID) ON DELETE CASCADE
);

ALTER TABLE Methods ADD UNIQUE IDX_Unique (ClassId, MethodName, Descriptor(255), Version);

CREATE INDEX IDX_Methods_MethodName
ON Methods (MethodName);

CREATE INDEX IDX_Methods_Version
ON Methods (Version);

CREATE INDEX IDX_Methods_Signature
ON Methods (Signature(255));

CREATE INDEX IDX_Methods_Exceptions
ON Methods (Exceptions(255));

CREATE INDEX IDX_Methods_Descriptor
ON Methods (Descriptor(255));

CREATE INDEX IDX_METHODS_IsAbstract
ON Methods (IsAbstract);

CREATE INDEX IDX_Methods_IsNative
ON Methods (IsNative);

CREATE INDEX IDX_Methods_IsPublic
ON Methods (IsPublic);





SET FOREIGN_KEY_CHECKS=0; 
DROP TABLE IF EXISTS Invocations;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE Invocations
(
ID int auto_increment PRIMARY KEY,
InvokeType varchar(15) NOT NULL,
CallerClass varchar(127) NOT NULL,
CallerMethod varchar(127) NOT NULL,
CallerMethodDesc text NOT NULL,
TargetClass varchar(127) NOT NULL,
TargetMethod varchar(127) NOT NULL,
TargetMethodDesc text NOT NULL,
Version varchar(15) NOT NULL
);

ALTER TABLE Invocations ADD UNIQUE IDX_Unique (CallerClass, CallerMethod, CallerMethodDesc(240), TargetClass, TargetMethod,TargetMethodDesc(240), Version);

CREATE INDEX IDX_Invocations_Version
ON Invocations (Version);

CREATE INDEX IDX_Invocations_InvokeType
ON Invocations (InvokeType);

CREATE INDEX IDX_Invocations_CallerClass
ON Invocations (CallerClass);

CREATE INDEX IDX_Invocations_TargetClass
ON Invocations (TargetClass);

CREATE INDEX IDX_Invocations_CallerMethod
ON Invocations (CallerMethod);

CREATE INDEX IDX_Invocations_TargetMethod
ON Invocations (TargetMethod);

CREATE INDEX IDX_Invocations_CallerMethodDesc
ON Invocations (TargetMethodDesc(255));

CREATE INDEX IDX_Invocations_TargetMethodDesc
ON Invocations (CallerMethodDesc(255));







SET FOREIGN_KEY_CHECKS=0; 
DROP TABLE IF EXISTS PermissionInvocations;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE PermissionInvocations
(
	InvocationId int NOT NULL,
	Permission varchar(127) NOT NULL,
	FOREIGN KEY (InvocationId) REFERENCES Invocations(ID) ON DELETE CASCADE
);

alter table PermissionInvocations add primary key (InvocationId, Permission);




DROP VIEW IF EXISTS vwMethods;

CREATE VIEW vwMethods AS
(
SELECT c.ID as ClassID, m.ID as MethodID, c.ClassName as ClassName, m.MethodName, m.Version, m.Access, m.Signature, m.Descriptor, m.Exceptions, m.IsAbstract, m.IsNative, m.IsPublic
FROM Methods m, Classes c
WHERE c.ID = m.ClassId
);

DROP VIEW IF EXISTS vwPublicMethods;

CREATE VIEW vwPublicMethods AS
(
SELECT m.*
FROM vwMethods m LEFT JOIN Classes c ON m.ClassID = c.ID
WHERE m.IsPublic = 1 AND c.IsPublic = 1
);



DROP VIEW IF EXISTS vwPermissionInvocation;
CREATE VIEW vwPermissionInvocation
AS
(
SELECT vm.Access, i.*, p.Permission FROM PermissionInvocations p left join Invocations i ON p.InvocationId = i.ID
	left join vwMethods vm ON i.CallerClass = vm.ClassName AND i.CallerMethod = vm.MethodName AND i.CallerMethodDesc = vm.Descriptor AND vm.Version = i.Version
);

DROP VIEW IF EXISTS vwPublicPermissionInvocation;
CREATE VIEW vwPublicPermissionInvocation
AS
(
SELECT p.*
FROM vwPermissionInvocation p
WHERE EXISTS (SELECT 1 FROM vwPublicMethods pm WHERE p.CallerClass = pm.ClassName AND p.CallerMethod = pm.MethodName AND p.CallerMethodDesc = pm.Descriptor AND p.Version = pm.Version)
);
