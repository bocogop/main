-- drop tables if needed - CPB
/*
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AppParameter' AND TABLE_SCHEMA = 'Core') drop table Core.AppParameter;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AppUserGlobalRole' AND TABLE_SCHEMA = 'Core') drop table Core.AppUserGlobalRole;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AppUserPreferences' AND TABLE_SCHEMA = 'Core') drop table Core.AppUserPreferences;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AuditLog' AND TABLE_SCHEMA = 'Core') drop table Core.AuditLog;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'RolePermission' AND TABLE_SCHEMA = 'Core') drop table Core.RolePermission;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Country' AND TABLE_SCHEMA = 'Core') drop table Core.Country;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'State' AND TABLE_SCHEMA = 'Core') drop table Core.State;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Role' AND TABLE_SCHEMA = 'Core') drop table Core.Role;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Permission' AND TABLE_SCHEMA = 'Core') drop table Core.Permission;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'AppUser' AND TABLE_SCHEMA = 'Core') drop table Core.AppUser;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Template' AND TABLE_SCHEMA = 'Core') drop table Core.Template;

if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Voter' AND TABLE_SCHEMA = 'dbo') drop table dbo.Voter;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Gender' AND TABLE_SCHEMA = 'dbo') drop table dbo.Gender;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Party' AND TABLE_SCHEMA = 'dbo') drop table dbo.Party;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Precinct' AND TABLE_SCHEMA = 'dbo') drop table dbo.Precinct;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Event' AND TABLE_SCHEMA = 'dbo') drop table dbo.Event;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Participation' AND TABLE_SCHEMA = 'dbo') drop table dbo.Participation;
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

CREATE TABLE [Core].[Country](
	[Id] int IDENTITY(1,1) NOT NULL,
	[Name] varchar(50) not null,
	[Code] varchar(10) not null,
	[FipsCode] varchar(5) not null,
	CONSTRAINT [PK_Country] PRIMARY KEY CLUSTERED ([Id] ASC)
)
GO

CREATE TABLE [Core].[State](
	[Id] int IDENTITY(1,1) NOT NULL,
	[Name] varchar(50) not null,
	[Code] varchar(10) not null,
	[FipsCode] varchar(5) not null,
	[CountryFK] int,
	CONSTRAINT [PK_State] PRIMARY KEY CLUSTERED ([Id] ASC)
)
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
	Nickname [varchar](100) null,
	VoterName as REPLACE(RTRIM(COALESCE(FirstName + ' ','') + COALESCE(MiddleName + ' ','') + COALESCE(LastName + ' ','') + COALESCE(NameSuffix + ' ','')), SPACE(2),SPACE(1)),
	DriversLicense [varchar](30) NULL,
	SSN [varchar](9) NULL,
	RegistrationDate date NULL,
	EffectiveDate date NULL,
	Phone [varchar](30) NULL,
	PhoneUserProvided [varchar](30) NULL,
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
	[EmailUserProvided] [varchar](255) NULL,
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
	[Nickname] [varchar](100) null,
	[VoterName]  [varchar](330) NULL,
	[DriversLicense] [varchar](30) NULL,
	[SSN] [varchar](9) NULL,
	[RegistrationDate] [date] NULL,
	[EffectiveDate] [date] NULL,
	[Phone] [varchar](30) NULL,
	[PhoneUserProvided] [varchar](255) NULL,
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
	[EmailUserProvided] [varchar](255) NULL,
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

create table dbo.Participation(
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[EventFK] int not null,
	[VoterFK] int not null,
	[CreatedBy] [varchar](30) NOT NULL,
	[CreatedDate] [datetime] NOT NULL,
	[ModifiedBy] [varchar](30) NOT NULL,
	[ModifiedDate] [datetime] NOT NULL,
	[Ver] [numeric](10, 0) NOT NULL,
	CONSTRAINT [PK_Participation] PRIMARY KEY CLUSTERED ([Id] ASC)
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

ALTER TABLE [Core].[State]  WITH CHECK ADD  CONSTRAINT [FK_State_Country] FOREIGN KEY([CountryFK])
REFERENCES [Core].[Country] ([Id])
GO
ALTER TABLE [Core].[State] CHECK CONSTRAINT [FK_State_Country]
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
CREATE NONCLUSTERED INDEX IX_Voter_LastName on [dbo].[Voter] (LastName);
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
CREATE UNIQUE NONCLUSTERED INDEX UQ_Participation ON [dbo].[Participation] (VoterFK, EventFK);
GO

---------------------------------------------- Triggers

CREATE TRIGGER dbo.[TR_Voter_INS_H] ON dbo.Voter
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
		   ,[NickName]
           ,[VoterName]
           ,[DriversLicense]
           ,[SSN]
           ,[RegistrationDate]
           ,[EffectiveDate]
           ,[Phone]
		   ,[PhoneUserProvided]
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
		   ,[EmailUserProvided]
		   ,[CreatedBy]
			,[CreatedDate]
			,[ModifiedBy]
			,[ModifiedDate]
			,[Ver])
SELECT 'I',SYSUTCDATETIME()
           ,i.*
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
		   ,[NickName]
           ,[VoterName]
           ,[DriversLicense]
           ,[SSN]
           ,[RegistrationDate]
           ,[EffectiveDate]
           ,[Phone]
		   ,[PhoneUserProvided]
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
		   ,[EmailUserProvided]
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
		   ,[NickName]
           ,[VoterName]
           ,[DriversLicense]
           ,[SSN]
           ,[RegistrationDate]
           ,[EffectiveDate]
           ,[Phone]
		   ,[PhoneUserProvided]
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
		   ,[EmailUserProvided]
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

set identity_insert core.Role on
GO
INSERT INTO [CORE].[Role](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [UsedAsPermissionInd], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (100, 'National_Administrator', 'National_Administrator', 1, '2000-01-01', null, 'N', 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
INSERT INTO [CORE].[Role](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [UsedAsPermissionInd], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (101, 'User', 'User', 2, '2000-01-01', null, 'N', 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
INSERT INTO [CORE].[Role](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [UsedAsPermissionInd], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (150, 'Voter', 'Voter', 3, '2000-01-01', null, 'N', 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
set identity_insert core.Role off
GO

set identity_insert core.Permission on
GO
INSERT INTO [CORE].[Permission](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (1, 'Login Application', 'Login Application', 1, '2000-01-01', null, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
INSERT INTO [CORE].[Permission](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (2, 'Manage Users', 'Manage Users', 2, '2000-01-01', null, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
INSERT INTO [CORE].[Permission](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (3, 'Assign Precincts', 'Assign Precincts', 3, '2000-01-01', null, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
INSERT INTO [CORE].[Permission](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (4, 'Edit Precincts', 'Edit Precincts', 4, '2000-01-01', null, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
INSERT INTO [CORE].[Permission](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (5, 'Edit Voters', 'Edit Voters', 5, '2000-01-01', null, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
INSERT INTO [CORE].[Permission](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (6, 'Edit Events', 'Edit Events', 6, '2000-01-01', null, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
INSERT INTO [CORE].[Permission](Id, [Name], [Description], [SortOrder], [EffectiveDate], [ExpirationDate], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES (50, 'Login Kiosk', 'Login Kiosk', 7, '2000-01-01', null, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME())
GO
set identity_insert core.Permission off
GO


INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 1, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 1, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 1, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 1, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 1, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 1, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
GO

INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(101, 1, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(101, 3, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(101, 4, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 5, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
GO

INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(150, 5, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(150, 50, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
GO

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

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.default.en', cast('Sorry, the logon credentials were invalid.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.expired.en', cast('Sorry, your account is expired. Please contact Boulder County GOP staff.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.disabled.en', cast('Sorry, your account is disabled. Please contact Boulder County GOP staff.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.locked.en', cast('Sorry, your account is locked. Please contact Boulder County GOP staff or wait 15 minutes for your account to unlock.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.default.es', cast('Lo sentimos, las credenciales de inicio de sesión no eran válidos.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.expired.es', cast('Lo sentimos, tu cuenta ha caducado. Por favor, póngase en contacto con el apoyo.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.disabled.es', cast('Lo sentimos, tu cuenta está deshabilitada. Por favor, póngase en contacto con soporte.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.locked.es', cast('Lo sentimos, tu cuenta está bloqueada. Por favor, póngase en contacto con el administrador de Boulder County o esperar 15 minutos para su cuenta para desbloquear.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('kiosk.globalIntroText.en', cast('Please enter your name and year of birth:' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('kiosk.globalIntroText.es', cast('Ingrese su nombre y año de nacimiento:' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('kiosk.homepageContent.en', cast('Thank you for checking into today''s event!<p>Please confirm your personal information and note upcoming opportunities below.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('kiosk.homepageContent.es', cast('
¡Gracias por visitar el evento de hoy!<p> Confirme su información personal y anote las próximas oportunidades a continuación.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('kiosk.homepageAnnouncement.en', cast('' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('kiosk.homepageAnnouncement.es', cast('' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

INSERT INTO [dbo].[Event]([Date]
           ,[Name]
           ,[CreatedBy]
           ,[CreatedDate]
           ,[ModifiedBy]
           ,[ModifiedDate]
           ,[Ver])
     VALUES
           ('2017-01-10'
           ,'Sample Event 1'
           ,'Initial Load'
           ,SYSUTCDATETIME()
           ,'Initial Load'
           ,SYSUTCDATETIME()
           ,1)
GO

INSERT INTO [dbo].[Event]
           ([Date]
           ,[Name]
           ,[CreatedBy]
           ,[CreatedDate]
           ,[ModifiedBy]
           ,[ModifiedDate]
           ,[Ver])
     VALUES
           ('2017-01-20'
           ,'Sample Event 2'
           ,'Initial Load'
           ,SYSUTCDATETIME()
           ,'Initial Load'
           ,SYSUTCDATETIME()
           ,1)
GO

set identity_insert core.Country on
GO
insert into [Core].[Country](Id, Name, Code, FipsCode) values(1,'United States','USA','US');
insert into [Core].[Country](Id, Name, Code, FipsCode) values(2,'Mexico','MEX','MX');
insert into [Core].[Country](Id, Name, Code, FipsCode) values(3,'Canada','CAN','CA');
set identity_insert core.Country off
GO

set identity_insert core.State on
GO
insert into core.State([Id], [Name],code, fipsCode) values(1,'Alabama','AL',1);
insert into core.State([Id], [Name],code, fipsCode) values(2,'Alaska','AK',2);
insert into core.State([Id], [Name],code, fipsCode) values(3,'Arizona','AZ',4);
insert into core.State([Id], [Name],code, fipsCode) values(4,'Arkansas','AR',5);
insert into core.State([Id], [Name],code, fipsCode) values(5,'California','CA',6);
insert into core.State([Id], [Name],code, fipsCode) values(6,'Colorado','CO',8);
insert into core.State([Id], [Name],code, fipsCode) values(7,'Connecticut','CT',9);
insert into core.State([Id], [Name],code, fipsCode) values(8,'Delaware','DE',10);
insert into core.State([Id], [Name],code, fipsCode) values(9,'District Of Columbia','DC',11);
insert into core.State([Id], [Name],code, fipsCode) values(10,'Florida','FL',12);
insert into core.State([Id], [Name],code, fipsCode) values(11,'Georgia','GA',13);
insert into core.State([Id], [Name],code, fipsCode) values(12,'Hawaii','HI',15);
insert into core.State([Id], [Name],code, fipsCode) values(13,'Idaho','ID',16);
insert into core.State([Id], [Name],code, fipsCode) values(14,'Illinois','IL',17);
insert into core.State([Id], [Name],code, fipsCode) values(15,'Indiana','IN',18);
insert into core.State([Id], [Name],code, fipsCode) values(16,'Iowa','IA',19);
insert into core.State([Id], [Name],code, fipsCode) values(17,'Kansas','KS',20);
insert into core.State([Id], [Name],code, fipsCode) values(18,'Kentucky','KY',21);
insert into core.State([Id], [Name],code, fipsCode) values(19,'Louisiana','LA',22);
insert into core.State([Id], [Name],code, fipsCode) values(20,'Maine','ME',23);
insert into core.State([Id], [Name],code, fipsCode) values(21,'Maryland','MD',24);
insert into core.State([Id], [Name],code, fipsCode) values(22,'Massachusetts','MA',25);
insert into core.State([Id], [Name],code, fipsCode) values(23,'Michigan','MI',26);
insert into core.State([Id], [Name],code, fipsCode) values(24,'Minnesota','MN',27);
insert into core.State([Id], [Name],code, fipsCode) values(25,'Mississippi','MS',28);
insert into core.State([Id], [Name],code, fipsCode) values(26,'Missouri','MO',29);
insert into core.State([Id], [Name],code, fipsCode) values(27,'Montana','MT',30);
insert into core.State([Id], [Name],code, fipsCode) values(28,'Nebraska','NE',31);
insert into core.State([Id], [Name],code, fipsCode) values(29,'Nevada','NV',32);
insert into core.State([Id], [Name],code, fipsCode) values(30,'New Hampshire','NH',33);
insert into core.State([Id], [Name],code, fipsCode) values(31,'New Jersey','NJ',34);
insert into core.State([Id], [Name],code, fipsCode) values(32,'New Mexico','NM',35);
insert into core.State([Id], [Name],code, fipsCode) values(33,'New York','NY',36);
insert into core.State([Id], [Name],code, fipsCode) values(34,'North Carolina','NC',37);
insert into core.State([Id], [Name],code, fipsCode) values(35,'North Dakota','ND',38);
insert into core.State([Id], [Name],code, fipsCode) values(36,'Ohio','OH',39);
insert into core.State([Id], [Name],code, fipsCode) values(37,'Oklahoma','OK',40);
insert into core.State([Id], [Name],code, fipsCode) values(38,'Oregon','OR',41);
insert into core.State([Id], [Name],code, fipsCode) values(39,'Pennsylvania','PA',42);
insert into core.State([Id], [Name],code, fipsCode) values(40,'Rhode Island','RI',44);
insert into core.State([Id], [Name],code, fipsCode) values(41,'South Carolina','SC',45);
insert into core.State([Id], [Name],code, fipsCode) values(42,'South Dakota','SD',46);
insert into core.State([Id], [Name],code, fipsCode) values(43,'Tennessee','TN',47);
insert into core.State([Id], [Name],code, fipsCode) values(44,'Texas','TX',48);
insert into core.State([Id], [Name],code, fipsCode) values(45,'Utah','UT',49);
insert into core.State([Id], [Name],code, fipsCode) values(46,'Vermont','VT',50);
insert into core.State([Id], [Name],code, fipsCode) values(47,'Virginia','VA',51);
insert into core.State([Id], [Name],code, fipsCode) values(48,'Washington','WA',53);
insert into core.State([Id], [Name],code, fipsCode) values(49,'West Virginia','WV',54);
insert into core.State([Id], [Name],code, fipsCode) values(50,'Wisconsin','WI',55);
insert into core.State([Id], [Name],code, fipsCode) values(51,'Wyoming','WY',56);
insert into core.State([Id], [Name],code, fipsCode) values(52,'Alberta','AB',58);
insert into core.State([Id], [Name],code, fipsCode) values(53,'British Columbia','BC',59);
insert into core.State([Id], [Name],code, fipsCode) values(54,'American Samoa','AS',60);
insert into core.State([Id], [Name],code, fipsCode) values(55,'Manitoba','MB',61);
insert into core.State([Id], [Name],code, fipsCode) values(56,'New Brunswick','NB',62);
insert into core.State([Id], [Name],code, fipsCode) values(57,'Newfoundland','NF',63);
insert into core.State([Id], [Name],code, fipsCode) values(58,'Federated States Of Micronesia','FM',64);
insert into core.State([Id], [Name],code, fipsCode) values(59,'Nova Scotia','NS',65);
insert into core.State([Id], [Name],code, fipsCode) values(60,'Guam','GU',66);
insert into core.State([Id], [Name],code, fipsCode) values(61,'Marshall Islands','MH',68);
insert into core.State([Id], [Name],code, fipsCode) values(62,'Northern Mariana Islands','MP',69);
insert into core.State([Id], [Name],code, fipsCode) values(63,'Palau','PW',70);
insert into core.State([Id], [Name],code, fipsCode) values(64,'Puerto Rico','PR',72);
insert into core.State([Id], [Name],code, fipsCode) values(65,'Northwest Territories','NT',73);
insert into core.State([Id], [Name],code, fipsCode) values(66,'U.S. Minor Outlying Islands','UM',74);
insert into core.State([Id], [Name],code, fipsCode) values(67,'Ontario','ON',75);
insert into core.State([Id], [Name],code, fipsCode) values(68,'US Misc Caribbean','US',76);
insert into core.State([Id], [Name],code, fipsCode) values(69,'Prince Edward Island','PE',77);
insert into core.State([Id], [Name],code, fipsCode) values(70,'Virgin Islands','VI',78);
insert into core.State([Id], [Name],code, fipsCode) values(71,'Quebec','QC',80);
insert into core.State([Id], [Name],code, fipsCode) values(72,'Saskatchewan','SK',82);
insert into core.State([Id], [Name],code, fipsCode) values(73,'Yukon Territory','YT',83);
insert into core.State([Id], [Name],code, fipsCode) values(74,'Armed Forces Amer (exc Canada)','AA',85);
insert into core.State([Id], [Name],code, fipsCode) values(75,'Armed Forces AF,EU,ME,CA','AE',87);
insert into core.State([Id], [Name],code, fipsCode) values(76,'Armed Forces Pacific','AP',88);
insert into core.State([Id], [Name],code, fipsCode) values(77,'Nunavut Province','NU',94);
insert into core.State([Id], [Name],code, fipsCode) values(78,'Philippines','PH',96);
insert into core.State([Id], [Name],code, fipsCode) values(79,'Aguascalientes','AGS.',1);
insert into core.State([Id], [Name],code, fipsCode) values(80,'Baja California Norte','B.C.',2);
insert into core.State([Id], [Name],code, fipsCode) values(81,'Baja California Sur','B.C.S.',3);
insert into core.State([Id], [Name],code, fipsCode) values(82,'Campeche','CAM.',4);
insert into core.State([Id], [Name],code, fipsCode) values(83,'Chiapas','CHIS.',5);
insert into core.State([Id], [Name],code, fipsCode) values(84,'Chihuahua','CHIH.',6);
insert into core.State([Id], [Name],code, fipsCode) values(85,'Coahuila','COAH.',7);
insert into core.State([Id], [Name],code, fipsCode) values(86,'Colima','COL.',8);
insert into core.State([Id], [Name],code, fipsCode) values(87,'Distrito Federal','D.F.',9);
insert into core.State([Id], [Name],code, fipsCode) values(88,'Durango','DGO.',10);
insert into core.State([Id], [Name],code, fipsCode) values(89,'Guanajuato','GTO.',11);
insert into core.State([Id], [Name],code, fipsCode) values(90,'Guerrero','GRO.',12);
insert into core.State([Id], [Name],code, fipsCode) values(91,'Hidalgo','HGO.',13);
insert into core.State([Id], [Name],code, fipsCode) values(92,'Jalisco','JAL.',14);
insert into core.State([Id], [Name],code, fipsCode) values(93,'México','MEX.',15);
insert into core.State([Id], [Name],code, fipsCode) values(94,'Michoacán','MICH.',16);
insert into core.State([Id], [Name],code, fipsCode) values(95,'Morelos','MOR.',17);
insert into core.State([Id], [Name],code, fipsCode) values(96,'Nayarit','NAY.',18);
insert into core.State([Id], [Name],code, fipsCode) values(97,'Nuevo León','N.L.',19);
insert into core.State([Id], [Name],code, fipsCode) values(98,'Oaxaca','OAX.',20);
insert into core.State([Id], [Name],code, fipsCode) values(99,'Puebla','PUE.',21);
insert into core.State([Id], [Name],code, fipsCode) values(100,'Querétaro','QRO.',22);
insert into core.State([Id], [Name],code, fipsCode) values(101,'Quintana Roo','Q. ROO.',23);
insert into core.State([Id], [Name],code, fipsCode) values(102,'San Luis Potosí','S.L.P.',24);
insert into core.State([Id], [Name],code, fipsCode) values(103,'Sinaloa','SIN.',25);
insert into core.State([Id], [Name],code, fipsCode) values(104,'Sonora','SON.',26);
insert into core.State([Id], [Name],code, fipsCode) values(105,'Tabasco','TAB.',27);
insert into core.State([Id], [Name],code, fipsCode) values(106,'Tamaulipas','TAMPS.',28);
insert into core.State([Id], [Name],code, fipsCode) values(107,'Tlaxcala','TLAX.',29);
insert into core.State([Id], [Name],code, fipsCode) values(108,'Veracruz','VER.',30);
insert into core.State([Id], [Name],code, fipsCode) values(109,'Yucatán','YUC.',31);
insert into core.State([Id], [Name],code, fipsCode) values(110,'Zacatecas','ZAC.',32);
insert into core.State([Id], [Name],code, fipsCode) values(111,'FOREIGN COUNTRY','FG',90);
insert into core.State([Id], [Name],code, fipsCode) values(112,'Other','OT',NULL);
insert into core.State([Id], [Name],code, fipsCode) values(113,'Unknown','UN',NULL);
set identity_insert core.State off
GO

update core.State set CountryFK = 1 where Id <= 51 or Id in (54,58,60,61,62,63, 64, 66, 68, 74,75,76,78 );
update core.State set CountryFK = 2 where CountryFK is null and Id >= 79 and Id <= 110;
update core.State set CountryFK = 3 where CountryFK is null and Id <> 111;
GO