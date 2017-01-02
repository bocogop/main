-- drop tables if needed - CPB
/*
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AppParameter' AND TABLE_SCHEMA = 'Core') drop table dbo.AppParameter;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AppUserGlobalRole' AND TABLE_SCHEMA = 'Core') drop table dbo.AppUserGlobalRole;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AppUserPreferences' AND TABLE_SCHEMA = 'Core') drop table dbo.AppUserPreferences;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AuditLog' AND TABLE_SCHEMA = 'Core') drop table dbo.AuditLog;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'RolePermission' AND TABLE_SCHEMA = 'Core') drop table dbo.RolePermission;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Role' AND TABLE_SCHEMA = 'Core') drop table dbo.Role;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Permission' AND TABLE_SCHEMA = 'Core') drop table dbo.Permission;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AppUser' AND TABLE_SCHEMA = 'Core') drop table dbo.AppUser;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Template' AND TABLE_SCHEMA = 'Core') drop table dbo.Template;

if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Voter' AND TABLE_SCHEMA = 'dbo') drop table dbo.Voter;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Gender' AND TABLE_SCHEMA = 'dbo') drop table dbo.Gender;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Party' AND TABLE_SCHEMA = 'dbo') drop table dbo.Party;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Precinct' AND TABLE_SCHEMA = 'dbo') drop table dbo.Precinct;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Event' AND TABLE_SCHEMA = 'dbo') drop table dbo.Event;
*/

CREATE TABLE [Core].[AppParameter](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[ParameterName] [varchar](80) NOT NULL,
	[ParameterValue] [varchar](255) NOT NULL,
	[CreatedBy] [varchar](30) NULL,
	[CreatedDate] [datetime] NULL,
	[ModifiedBy] [varchar](30) NULL,
	[ModifiedDate] [datetime] NULL,
	[Ver] [numeric](10, 0) NOT NULL,
 CONSTRAINT [PK_APP_PARAMETER] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [Core].[AppUser](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[Username] [varchar](20) NULL,
	[PasswordHash] [varchar](250) NULL,
	[TimeZone] [varchar](50) NULL,
	[FirstName] [varchar](50) NULL,
	[MiddleName] [varchar](50) NULL,
	[LastName] [varchar](50) NULL,
	[Description] [varchar](255) NULL,
	[Phone] [varchar](30) NULL,
	[Email] [varchar](255) NULL,
	[EnabledInd] [char](1) NOT NULL,
	[CreatedBy] [varchar](30) NULL,
	[CreatedDate] [datetime] NULL,
	[ModifiedBy] [varchar](30) NULL,
	[ModifiedDate] [datetime] NULL,
	[Ver] [numeric](10, 0) NOT NULL,
 CONSTRAINT [PK__APP_USER] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [Core].[AppUserGlobalRole](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[AppUserFK] [numeric](18, 0) NOT NULL,
	[RoleFK] [numeric](18, 0) NOT NULL,
	[CreatedBy] [varchar](30) NULL,
	[CreatedDate] [datetime] NULL,
	[ModifiedBy] [varchar](30) NULL,
	[ModifiedDate] [datetime] NULL,
 CONSTRAINT [PK_AppUserGlobalRole] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [Core].[AppUserPreferences](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[AppUserFK] [numeric](18, 0) NOT NULL,
	[Ver] [numeric](10, 0) NOT NULL,
	[CreatedBy] [varchar](30) NOT NULL,
	[CreatedDate] [datetime] NOT NULL,
	[ModifiedBy] [varchar](30) NOT NULL,
	[ModifiedDate] [datetime] NOT NULL,
 CONSTRAINT [XPK_AppUserPreferences] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [Core].[AuditLog](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[AppUserFK] [varchar](512) NOT NULL,
	[ExecutionDate] [datetime] NOT NULL,
	[MethodName] [varchar](512) NOT NULL,
	[ParameterValues] [varchar](max) NULL,
	[CreatedBy] [varchar](30) NOT NULL,
	[CreatedDate] [datetime] NOT NULL,
	[ModifiedBy] [varchar](30) NOT NULL,
	[ModifiedDate] [datetime] NOT NULL,
 CONSTRAINT [XPK_AuditLog] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [Core].[Permission](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[Name] [varchar](80) NOT NULL,
	[Description] [varchar](255) NOT NULL,
	[SortOrder] [numeric](6, 0) NULL,
	[EffectiveDate] [datetime] NOT NULL,
	[ExpirationDate] [datetime] NULL,
	[CreatedBy] [varchar](30) NULL,
	[CreatedDate] [datetime] NULL,
	[ModifiedBy] [varchar](30) NULL,
	[ModifiedDate] [datetime] NULL,
 CONSTRAINT [PK_Permission] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [Core].[Role](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[Name] [varchar](80) NOT NULL,
	[Description] [varchar](255) NOT NULL,
	[SortOrder] [numeric](6, 0) NULL,
	[EffectiveDate] [datetime] NOT NULL,
	[ExpirationDate] [datetime] NULL,
	[UsedAsPermissionInd] [char](1) NOT NULL,
	[CreatedBy] [varchar](30) NULL,
	[CreatedDate] [datetime] NULL,
	[ModifiedBy] [varchar](30) NULL,
	[ModifiedDate] [datetime] NULL,
 CONSTRAINT [PK_Role] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [Core].[RolePermission](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[RoleFK] [numeric](18, 0) NOT NULL,
	[PermissionFK] [numeric](18, 0) NOT NULL,
	[CreatedBy] [varchar](30) NULL,
	[CreatedDate] [datetime] NULL,
	[ModifiedBy] [varchar](30) NULL,
	[ModifiedDate] [datetime] NULL,
 CONSTRAINT [PK_RolePermission] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]
GO

CREATE TABLE [Core].[Template](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[TemplateName] [varchar](40) NOT NULL,
	[TemplateBody] [varbinary](max) NOT NULL,
	[CreatedBy] [varchar](30) NOT NULL,
	[CreatedDate] [datetime] NOT NULL,
	[ModifiedBy] [varchar](30) NOT NULL,
	[ModifiedDate] [datetime] NOT NULL,
	[Ver] [numeric](10, 0) NOT NULL,
 CONSTRAINT [XPK_Template] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

create table dbo.Precinct(
	[Id] [int] IDENTITY(1,1) NOT NULL,
	Code varchar(20) not null,
	Name varchar(40) not null,
	CONSTRAINT [PK_Precinct] PRIMARY KEY CLUSTERED ([Id] ASC)
)
-- Precinct data will be populated on first run of the transform script - CPB

create table dbo.Party(
	[Id] [int] IDENTITY(1,1) NOT NULL,
	Code varchar(3) not null,
	Name varchar(40) not null,
	CONSTRAINT [PK_party] PRIMARY KEY CLUSTERED ([Id] ASC)
)

create table dbo.Gender(
	[Id] [int] IDENTITY(1,1) NOT NULL,
	Code varchar(3) not null,
	Name varchar(40) not null,
	CONSTRAINT [PK_Gender] PRIMARY KEY CLUSTERED ([Id] ASC)
)

CREATE TABLE [dbo].[Voter](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	VoterId [varchar](255) NOT NULL,
	FirstName [varchar](100) NULL,
	MiddleName [varchar](100) NULL,
	LastName [varchar](100) NULL,
	NameSuffix [varchar](20) NULL,
	VoterName as REPLACE(RTRIM(COALESCE(FirstName + ' ','') + COALESCE(MiddleName + ' ','') + COALESCE(LastName + ' ','') + COALESCE(NameSuffix + ' ','')), SPACE(2),SPACE(1)),
	DriversLicense [varchar](30) NULL,
	SSN [varchar](9) NULL,
	RegistrationDate date NULL,
	EffectiveDate date NULL,
	Phone [varchar](30) NULL,
	HouseNumber [varchar](10) NULL,
	HouseSuffix [varchar](20) NULL,
	PreDirection [varchar](5) NULL,
	StreetName [varchar](255) NULL,
	StreetType [varchar](10) NULL,
	PostDirection [varchar](255) NULL,
	UnitType [varchar](20) NULL,
	UnitNumber [varchar](15) NULL,
	ResidentialAddress [varchar](255) NULL,
	ResidentialCity [varchar](255) NULL,
	ResidentialState [varchar](2) NULL,
	ResidentialZip [varchar](15) NULL,
	ResidentialZipPlus [varchar](20) NULL,
	MailingAddress1 [varchar](255) NULL,
	MailingAddress2 [varchar](255) NULL,
	MailingAddress3 [varchar](255) NULL,
	MailingCity [varchar](255) NULL,
	MailingState [varchar](2) NULL,
	MailingZip [varchar](15) NULL,
	MailingZipPlus [varchar](20) NULL,
	MailingCountry [varchar](100) NULL,
	BallotAddress1 [varchar](255) NULL,
	BallotAddress2 [varchar](255) NULL,
	BallotAddress3 [varchar](255) NULL,
	BallotCity [varchar](255) NULL,
	BallotState [varchar](2) NULL,
	BallotZip [varchar](15) NULL,
	BallotZipPlus [varchar](20) NULL,
	BallotCountry [varchar](100) NULL,
	VoterStatusActive bit NULL,
	VoterStatusReason [varchar](255) NULL,
	PartyFK int null,
	AffiliatedDate date NULL,
	GenderFK int NULL,
	IdRequired bit NULL,
	BirthYear int NULL,
	PrecinctFK int null,
	[UOCAVA] bit NULL,
	IssueMethod [varchar](255) NULL,
	Fax [varchar](255) NULL,
	Email [varchar](255) NULL,
	[CreatedBy] [varchar](30) NOT NULL,
	[CreatedDate] [datetime] NOT NULL,
	[ModifiedBy] [varchar](30) NOT NULL,
	[ModifiedDate] [datetime] NOT NULL,
	[Ver] [numeric](10, 0) NOT NULL,
 CONSTRAINT [PK_voter] PRIMARY KEY CLUSTERED ([Id] ASC)
	WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)
GO

CREATE TABLE [dbo].[Voter_H](
	[Id_H] [int] IDENTITY(1,1) NOT NULL,
	[TransactionCode] char(1) not null,
	[ModifiedDate_H] datetime not null,
	[Id] [int] not null,
	[VoterId] [varchar](255) NOT NULL,
	[FirstName] [varchar](100) NULL,
	[MiddleName] [varchar](100) NULL,
	[LastName] [varchar](100) NULL,
	[NameSuffix] [varchar](20) NULL,
	[VoterName]  [varchar](330) NULL,
	[DriversLicense] [varchar](30) NULL,
	[SSN] [varchar](9) NULL,
	[RegistrationDate] [date] NULL,
	[EffectiveDate] [date] NULL,
	[Phone] [varchar](30) NULL,
	[HouseNumber] [varchar](10) NULL,
	[HouseSuffix] [varchar](20) NULL,
	[PreDirection] [varchar](5) NULL,
	[StreetName] [varchar](255) NULL,
	[StreetType] [varchar](10) NULL,
	[PostDirection] [varchar](255) NULL,
	[UnitType] [varchar](20) NULL,
	[UnitNumber] [varchar](15) NULL,
	[ResidentialAddress] [varchar](255) NULL,
	[ResidentialCity] [varchar](255) NULL,
	[ResidentialState] [varchar](2) NULL,
	[ResidentialZip] [varchar](15) NULL,
	[ResidentialZipPlus] [varchar](20) NULL,
	[MailingAddress1] [varchar](255) NULL,
	[MailingAddress2] [varchar](255) NULL,
	[MailingAddress3] [varchar](255) NULL,
	[MailingCity] [varchar](255) NULL,
	[MailingState] [varchar](2) NULL,
	[MailingZip] [varchar](15) NULL,
	[MailingZipPlus] [varchar](20) NULL,
	[MailingCountry] [varchar](100) NULL,
	[BallotAddress1] [varchar](255) NULL,
	[BallotAddress2] [varchar](255) NULL,
	[BallotAddress3] [varchar](255) NULL,
	[BallotCity] [varchar](255) NULL,
	[BallotState] [varchar](2) NULL,
	[BallotZip] [varchar](15) NULL,
	[BallotZipPlus] [varchar](20) NULL,
	[BallotCountry] [varchar](100) NULL,
	[VoterStatusActive] [bit] NULL,
	[VoterStatusReason] [varchar](255) NULL,
	[PartyFK] [int] NULL,
	[AffiliatedDate] [date] NULL,
	[GenderFK] [int] NULL,
	[IdRequired] [bit] NULL,
	[BirthYear] [int] NULL,
	[PrecinctFK] [int] NULL,
	[UOCAVA] [bit] NULL,
	[IssueMethod] [varchar](255) NULL,
	[Fax] [varchar](255) NULL,
	[Email] [varchar](255) NULL,
	[CreatedBy] [varchar](30) NOT NULL,
	[CreatedDate] [datetime] NOT NULL,
	[ModifiedBy] [varchar](30) NOT NULL,
	[ModifiedDate] [datetime] NOT NULL,
	[Ver] [numeric](10, 0) NOT NULL,
	CONSTRAINT [PK_voter_H] PRIMARY KEY CLUSTERED ([Id_H] ASC)
)
GO

create table dbo.Event(
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Date] date not null,
	Name varchar(100) not null,
	[CreatedBy] [varchar](30) NOT NULL,
	[CreatedDate] [datetime] NOT NULL,
	[ModifiedBy] [varchar](30) NOT NULL,
	[ModifiedDate] [datetime] NOT NULL,
	[Ver] [numeric](10, 0) NOT NULL,
	CONSTRAINT [PK_Event] PRIMARY KEY CLUSTERED ([Id] ASC)
)
GO

----------------------------------------- Constraints

ALTER TABLE [Core].[AppUser] ADD  DEFAULT ('N') FOR [EnabledInd]
GO

ALTER TABLE [Core].[AppUser] ADD  DEFAULT ((0)) FOR [Ver]
GO

ALTER TABLE [Core].[AppUserGlobalRole]  WITH CHECK ADD  CONSTRAINT [FK_AppUserGlobalRole_AppUser] FOREIGN KEY([AppUserFK])
REFERENCES [Core].[AppUser] ([Id])
GO

ALTER TABLE [Core].[AppUserGlobalRole] CHECK CONSTRAINT [FK_AppUserGlobalRole_AppUser]
GO

ALTER TABLE [Core].[AppUserGlobalRole]  WITH CHECK ADD  CONSTRAINT [FK_AppUserGlobalRole_Role] FOREIGN KEY([RoleFK])
REFERENCES [Core].[Role] ([Id])
GO

ALTER TABLE [Core].[AppUserGlobalRole] CHECK CONSTRAINT [FK_AppUserGlobalRole_Role]
GO

ALTER TABLE [Core].[AppUserPreferences] ADD  CONSTRAINT [DF_APP_USER_PREFERENCES_Ver]  DEFAULT ((0)) FOR [Ver]
GO

ALTER TABLE [Core].[AppUserPreferences]  WITH NOCHECK ADD  CONSTRAINT [FK_AppUserPref_AppUser] FOREIGN KEY([AppUserFK])
REFERENCES [Core].[AppUser] ([Id])
GO

ALTER TABLE [Core].[AppUserPreferences] CHECK CONSTRAINT [FK_AppUserPref_AppUser]
GO

ALTER TABLE [Core].[Role] ADD  DEFAULT ('N') FOR [UsedAsPermissionInd]
GO

ALTER TABLE [Core].[RolePermission]  WITH CHECK ADD  CONSTRAINT [FK_RolePermission_Permission] FOREIGN KEY([PermissionFK])
REFERENCES [Core].[Permission] ([Id])
GO

ALTER TABLE [Core].[RolePermission] CHECK CONSTRAINT [FK_RolePermission_Permission]
GO

ALTER TABLE [Core].[RolePermission]  WITH CHECK ADD  CONSTRAINT [FK_RolePermission_Role] FOREIGN KEY([RoleFK])
REFERENCES [Core].[Role] ([Id])
GO

ALTER TABLE [Core].[RolePermission] CHECK CONSTRAINT [FK_RolePermission_Role]
GO

ALTER TABLE [Core].[Template] ADD CONSTRAINT [DF_Template_Ver]  DEFAULT ((0)) FOR [Ver]
GO

ALTER TABLE [dbo].[Voter] WITH CHECK ADD CONSTRAINT [FK_Voter_Party] FOREIGN KEY (PartyFK) REFERENCES [dbo].Party([Id]);
ALTER TABLE [dbo].[Voter] CHECK CONSTRAINT [FK_Voter_Party];

ALTER TABLE [dbo].[Voter] WITH CHECK ADD CONSTRAINT [FK_Voter_Gender] FOREIGN KEY (GenderFK) REFERENCES [dbo].Gender([Id]);
ALTER TABLE [dbo].[Voter] CHECK CONSTRAINT [FK_Voter_Gender];

ALTER TABLE [dbo].[Voter] WITH CHECK ADD CONSTRAINT [FK_Voter_Precinct] FOREIGN KEY (PrecinctFK) REFERENCES [dbo].Precinct([Id]);
ALTER TABLE [dbo].[Voter] CHECK CONSTRAINT [FK_Voter_Precinct];

---------------------------------------------- Indexes

CREATE UNIQUE NONCLUSTERED INDEX UQ_VoterId ON [dbo].[Voter] (VoterId);
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_PrecinctCode ON [dbo].Precinct (Code);
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_PartyCode ON [dbo].Party (Code);
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_GenderName ON [dbo].Gender (Name);
GO
CREATE NONCLUSTERED INDEX [IX_Voter_H_Id] ON [dbo].[Voter_H]([Id] ASC)
GO
CREATE NONCLUSTERED INDEX [IX_Voter_H_VoterId] ON [dbo].[Voter_H]([VoterId] ASC)
GO

---------------------------------------------- Triggers

CREATE TRIGGER [Core].[TR_Voter_INS_H] ON dbo.Voter
WITH EXEC AS CALLER
AFTER INSERT
AS
BEGIN
INSERT INTO dbo.Voter_H
    ([TransactionCode]
			,[ModifiedDate_H]
           ,[Id]
           ,[VoterId]
           ,[FirstName]
           ,[MiddleName]
           ,[LastName]
           ,[NameSuffix]
           ,[VoterName]
           ,[DriversLicense]
           ,[SSN]
           ,[RegistrationDate]
           ,[EffectiveDate]
           ,[Phone]
           ,[HouseNumber]
           ,[HouseSuffix]
           ,[PreDirection]
           ,[StreetName]
           ,[StreetType]
           ,[PostDirection]
           ,[UnitType]
           ,[UnitNumber]
           ,[ResidentialAddress]
           ,[ResidentialCity]
           ,[ResidentialState]
           ,[ResidentialZip]
           ,[ResidentialZipPlus]
           ,[MailingAddress1]
           ,[MailingAddress2]
           ,[MailingAddress3]
           ,[MailingCity]
           ,[MailingState]
           ,[MailingZip]
           ,[MailingZipPlus]
           ,[MailingCountry]
           ,[BallotAddress1]
           ,[BallotAddress2]
           ,[BallotAddress3]
           ,[BallotCity]
           ,[BallotState]
           ,[BallotZip]
           ,[BallotZipPlus]
           ,[BallotCountry]
           ,[VoterStatusActive]
           ,[VoterStatusReason]
           ,[PartyFK]
           ,[AffiliatedDate]
           ,[GenderFK]
           ,[IdRequired]
           ,[BirthYear]
           ,[PrecinctFK]
           ,[UOCAVA]
           ,[IssueMethod]
           ,[Fax]
           ,[Email]
		   ,[CreatedBy]
			,[CreatedDate]
			,[ModifiedBy]
			,[ModifiedDate]
			,[Ver])
SELECT 'I',SYSUTCDATETIME(), i.*
  FROM inserted i
END
GO

ALTER TABLE dbo.Voter ENABLE TRIGGER [TR_Voter_INS_H]
GO

--
CREATE TRIGGER dbo.TR_Voter_UPD_H ON dbo.Voter
WITH EXEC AS CALLER
AFTER UPDATE
AS
BEGIN
INSERT INTO dbo.Voter_H
    ([TransactionCode]
			,[ModifiedDate_H]
           ,[Id]
           ,[VoterId]
           ,[FirstName]
           ,[MiddleName]
           ,[LastName]
           ,[NameSuffix]
           ,[VoterName]
           ,[DriversLicense]
           ,[SSN]
           ,[RegistrationDate]
           ,[EffectiveDate]
           ,[Phone]
           ,[HouseNumber]
           ,[HouseSuffix]
           ,[PreDirection]
           ,[StreetName]
           ,[StreetType]
           ,[PostDirection]
           ,[UnitType]
           ,[UnitNumber]
           ,[ResidentialAddress]
           ,[ResidentialCity]
           ,[ResidentialState]
           ,[ResidentialZip]
           ,[ResidentialZipPlus]
           ,[MailingAddress1]
           ,[MailingAddress2]
           ,[MailingAddress3]
           ,[MailingCity]
           ,[MailingState]
           ,[MailingZip]
           ,[MailingZipPlus]
           ,[MailingCountry]
           ,[BallotAddress1]
           ,[BallotAddress2]
           ,[BallotAddress3]
           ,[BallotCity]
           ,[BallotState]
           ,[BallotZip]
           ,[BallotZipPlus]
           ,[BallotCountry]
           ,[VoterStatusActive]
           ,[VoterStatusReason]
           ,[PartyFK]
           ,[AffiliatedDate]
           ,[GenderFK]
           ,[IdRequired]
           ,[BirthYear]
           ,[PrecinctFK]
           ,[UOCAVA]
           ,[IssueMethod]
           ,[Fax]
           ,[Email]
		   ,[CreatedBy]
			,[CreatedDate]
			,[ModifiedBy]
			,[ModifiedDate]
			,[Ver])
SELECT 'U',SYSUTCDATETIME(), i.*
  FROM inserted i
END
GO

ALTER TABLE dbo.Voter ENABLE TRIGGER TR_Voter_UPD_H
GO

--
CREATE TRIGGER dbo.TR_Voter_DEL_H ON dbo.Voter
WITH EXEC AS CALLER
AFTER DELETE
AS
BEGIN
INSERT INTO dbo.Voter_H
    ([TransactionCode]
			,[ModifiedDate_H]
           ,[Id]
           ,[VoterId]
           ,[FirstName]
           ,[MiddleName]
           ,[LastName]
           ,[NameSuffix]
           ,[VoterName]
           ,[DriversLicense]
           ,[SSN]
           ,[RegistrationDate]
           ,[EffectiveDate]
           ,[Phone]
           ,[HouseNumber]
           ,[HouseSuffix]
           ,[PreDirection]
           ,[StreetName]
           ,[StreetType]
           ,[PostDirection]
           ,[UnitType]
           ,[UnitNumber]
           ,[ResidentialAddress]
           ,[ResidentialCity]
           ,[ResidentialState]
           ,[ResidentialZip]
           ,[ResidentialZipPlus]
           ,[MailingAddress1]
           ,[MailingAddress2]
           ,[MailingAddress3]
           ,[MailingCity]
           ,[MailingState]
           ,[MailingZip]
           ,[MailingZipPlus]
           ,[MailingCountry]
           ,[BallotAddress1]
           ,[BallotAddress2]
           ,[BallotAddress3]
           ,[BallotCity]
           ,[BallotState]
           ,[BallotZip]
           ,[BallotZipPlus]
           ,[BallotCountry]
           ,[VoterStatusActive]
           ,[VoterStatusReason]
           ,[PartyFK]
           ,[AffiliatedDate]
           ,[GenderFK]
           ,[IdRequired]
           ,[BirthYear]
           ,[PrecinctFK]
           ,[UOCAVA]
           ,[IssueMethod]
           ,[Fax]
           ,[Email]
		   ,[CreatedBy]
			,[CreatedDate]
			,[ModifiedBy]
			,[ModifiedDate]
			,[Ver])
SELECT 'D',SYSUTCDATETIME(), i.*
  FROM deleted i
END
GO

ALTER TABLE dbo.Voter ENABLE TRIGGER TR_Voter_DEL_H
GO

---------------------------------------------- Initial Data

/*	we only get new codes from the state so we'll just
	populate these names as 'unknown' in the transform script and manually
	update the names later - CPB */
insert into dbo.Party(Code, Name) values('UAF', 'Unaffiliated');
insert into dbo.Party(Code, Name) values('LBR', 'Libertarian');
insert into dbo.Party(Code, Name) values('GRN', 'Green');
insert into dbo.Party(Code, Name) values('DEM', 'Democratic');
insert into dbo.Party(Code, Name) values('ACN', 'American Constitution');
insert into dbo.Party(Code, Name) values('UNI', 'Unity');
insert into dbo.Party(Code, Name) values('REP', 'Republican');
GO

/*	we only get new names from the state so we'll just
	populate the codes as '?' in the transform script and manually
	update the codes later - CPB */
insert into dbo.Gender(Code, Name) values('F', 'Female');
insert into dbo.Gender(Code, Name) values('M', 'Male');
insert into dbo.Gender(Code, Name) values('U', 'Unknown');
GO

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('refDataLinks', cast('<li id="refMainMenu" role="menuitem" aria-haspopup="true">
<a href="#" title="sub menu">Policies</a>
<ul id="refPoliciesMenu" role="menu">
        <li role="menuitem"><a href="http://www.google.com" target="_blank">Google</a></li>
</ul></li>
<li role="menuitem"><a href="http://www.bocogop.org" target="_blank">Boulder County GOP Website</a></li>' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('footerContent', cast(' <ul style="padding-top: 5px; padding-bottom: 5px; margin-top: 0px; margin-bottom: 0px">
    <li><span style="color: red"><b>WARNING:</b> All
                    actions are audited and reported by this application.
    </span></li>
    <li>If you are experiencing problems or have a question about this website, please contact the <a href="mailto:bocogop@slickapps.com">BOCOGOP data team</a>.</li>
</ul>' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('systemNotification', cast('' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);