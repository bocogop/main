-- drop tables if needed - CPB
/*
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Voter' AND TABLE_SCHEMA = 'dbo') drop table dbo.Voter;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Gender' AND TABLE_SCHEMA = 'dbo') drop table dbo.Gender;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Party' AND TABLE_SCHEMA = 'dbo') drop table dbo.Party;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Precinct' AND TABLE_SCHEMA = 'dbo') drop table dbo.Precinct;
*/

CREATE TABLE [CORE].[APP_PARAMETER](
	[APP_PARAMETER_ID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[PARAMETER_NAME] [varchar](80) NOT NULL,
	[PARAMETER_VALUE] [varchar](255) NOT NULL,
	[CREATED_BY] [varchar](30) NULL,
	[CREATED_DATE] [datetime] NULL,
	[MODIFIED_BY] [varchar](30) NULL,
	[MODIFIED_DATE] [datetime] NULL,
	[Ver] [numeric](10, 0) NOT NULL,
 CONSTRAINT [PK__APP_PARAMETER] PRIMARY KEY CLUSTERED 
(
	[APP_PARAMETER_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [CORE].[APP_USER](
	[APP_USER_ID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[USERNAME] [varchar](20) NULL,
	[PASSWORD_HASH] [varchar](250) NULL,
	[TIME_ZONE] [varchar](50) NULL,
	[FIRST_NAME] [varchar](50) NULL,
	[MIDDLE_NAME] [varchar](50) NULL,
	[LAST_NAME] [varchar](50) NULL,
	[DESCRIPTION] [varchar](255) NULL,
	[PHONE] [varchar](30) NULL,
	[EMAIL] [varchar](255) NULL,
	[LAST_VISITED_PRECINCT_ID] [numeric](18, 0) NULL,
	[ENABLED_IND] [char](1) NOT NULL,
	[CREATED_BY] [varchar](30) NULL,
	[CREATED_DATE] [datetime] NULL,
	[MODIFIED_BY] [varchar](30) NULL,
	[MODIFIED_DATE] [datetime] NULL,
	[Ver] [numeric](10, 0) NOT NULL,
 CONSTRAINT [PK__APP_USER] PRIMARY KEY CLUSTERED 
(
	[APP_USER_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [CORE].[APP_USER_GLOBAL_ROLE](
	[APP_USER_GLOBAL_ROLE_ID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[APP_USER_ID] [numeric](18, 0) NOT NULL,
	[ROLE_ID] [numeric](18, 0) NOT NULL,
	[CREATED_BY] [varchar](30) NULL,
	[CREATED_DATE] [datetime] NULL,
	[MODIFIED_BY] [varchar](30) NULL,
	[MODIFIED_DATE] [datetime] NULL,
 CONSTRAINT [PK__APP_USER_GLOBAL_ROLE] PRIMARY KEY CLUSTERED 
(
	[APP_USER_GLOBAL_ROLE_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [CORE].[APP_USER_PREFERENCES](
	[ID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[APP_USER_ID] [numeric](18, 0) NOT NULL,
	[Ver] [numeric](10, 0) NOT NULL,
	[CREATED_BY] [varchar](30) NOT NULL,
	[CREATED_DATE] [datetime] NOT NULL,
	[MODIFIED_BY] [varchar](30) NOT NULL,
	[MODIFIED_DATE] [datetime] NOT NULL,
 CONSTRAINT [XPK_APP_USER_PREFERENCES] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [CORE].[AUDIT_LOG](
	[ID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[APP_USER_ID] [varchar](512) NOT NULL,
	[EXECUTION_DATE] [datetime] NOT NULL,
	[METHOD_NAME] [varchar](512) NOT NULL,
	[PARAMETER_VALUES] [varchar](max) NULL,
	[CREATED_BY] [varchar](30) NOT NULL,
	[CREATED_DATE] [datetime] NOT NULL,
	[MODIFIED_BY] [varchar](30) NOT NULL,
	[MODIFIED_DATE] [datetime] NOT NULL,
 CONSTRAINT [XPKAUDIT_LOG] PRIMARY KEY CLUSTERED 
(
	[ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

CREATE TABLE [CORE].[PERMISSION](
	[PERMISSION_ID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[NAME] [varchar](80) NOT NULL,
	[DESCRIPTION] [varchar](255) NOT NULL,
	[SORT_ORDER] [numeric](6, 0) NULL,
	[EFFECTIVE_DATE] [datetime] NOT NULL,
	[EXPIRATION_DATE] [datetime] NULL,
	[CREATED_BY] [varchar](30) NULL,
	[CREATED_DATE] [datetime] NULL,
	[MODIFIED_BY] [varchar](30) NULL,
	[MODIFIED_DATE] [datetime] NULL,
 CONSTRAINT [PK__PERMISSION] PRIMARY KEY CLUSTERED 
(
	[PERMISSION_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [CORE].[ROLE](
	[ROLE_ID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[NAME] [varchar](80) NOT NULL,
	[DESCRIPTION] [varchar](255) NOT NULL,
	[SORT_ORDER] [numeric](6, 0) NULL,
	[EFFECTIVE_DATE] [datetime] NOT NULL,
	[EXPIRATION_DATE] [datetime] NULL,
	[USED_AS_PERMISSION_IND] [char](1) NOT NULL,
	[CREATED_BY] [varchar](30) NULL,
	[CREATED_DATE] [datetime] NULL,
	[MODIFIED_BY] [varchar](30) NULL,
	[MODIFIED_DATE] [datetime] NULL,
 CONSTRAINT [PK__ROLE] PRIMARY KEY CLUSTERED 
(
	[ROLE_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [CORE].[ROLE_PERMISSION](
	[ROLE_PERMISSION_ID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[ROLE_ID] [numeric](18, 0) NOT NULL,
	[PERMISSION_ID] [numeric](18, 0) NOT NULL,
	[CREATED_BY] [varchar](30) NULL,
	[CREATED_DATE] [datetime] NULL,
	[MODIFIED_BY] [varchar](30) NULL,
	[MODIFIED_DATE] [datetime] NULL,
 CONSTRAINT [PK__ROLE_PERMISSION] PRIMARY KEY CLUSTERED 
(
	[ROLE_PERMISSION_ID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, FILLFACTOR = 80) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [CORE].[STD_TEMPLATE](
	[STD_TEMPLATE_ID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[TEMPLATE_NAME] [varchar](40) NOT NULL,
	[TEMPLATE_BODY] [varbinary](max) NOT NULL,
	[CREATED_BY] [varchar](30) NOT NULL,
	[CREATED_DATE] [datetime] NOT NULL,
	[MODIFIED_BY] [varchar](30) NOT NULL,
	[MODIFIED_DATE] [datetime] NOT NULL,
	[Ver] [numeric](10, 0) NOT NULL,
 CONSTRAINT [XPKPCM_STD_TEMPLATE] PRIMARY KEY CLUSTERED 
(
	[STD_TEMPLATE_ID] ASC
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
 CONSTRAINT [PK_voter] PRIMARY KEY CLUSTERED ([Id] ASC)
	WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)
GO

----------------------------------------- Constraints

ALTER TABLE [dbo].[Voter] WITH CHECK ADD CONSTRAINT [FK_Voter_Party] FOREIGN KEY (PartyFK) REFERENCES [dbo].Party([Id]);
ALTER TABLE [dbo].[Voter] CHECK CONSTRAINT [FK_Voter_Party];

ALTER TABLE [dbo].[Voter] WITH CHECK ADD CONSTRAINT [FK_Voter_Gender] FOREIGN KEY (GenderFK) REFERENCES [dbo].Gender([Id]);
ALTER TABLE [dbo].[Voter] CHECK CONSTRAINT [FK_Voter_Gender];

ALTER TABLE [dbo].[Voter] WITH CHECK ADD CONSTRAINT [FK_Voter_Precinct] FOREIGN KEY (PrecinctFK) REFERENCES [dbo].Precinct([Id]);
ALTER TABLE [dbo].[Voter] CHECK CONSTRAINT [FK_Voter_Precinct];

ALTER TABLE [CORE].[APP_USER] ADD  DEFAULT ('N') FOR [ENABLED_IND]
GO

ALTER TABLE [CORE].[APP_USER] ADD  DEFAULT ((0)) FOR [Ver]
GO

ALTER TABLE [CORE].[APP_USER_GLOBAL_ROLE]  WITH CHECK ADD  CONSTRAINT [FK_AppUserGlobalRole_AppUser] FOREIGN KEY([APP_USER_ID])
REFERENCES [CORE].[APP_USER] ([APP_USER_ID])
GO

ALTER TABLE [CORE].[APP_USER_GLOBAL_ROLE] CHECK CONSTRAINT [FK_AppUserGlobalRole_AppUser]
GO

ALTER TABLE [CORE].[APP_USER_GLOBAL_ROLE]  WITH CHECK ADD  CONSTRAINT [FK_AppUserGlobalRole_Role] FOREIGN KEY([ROLE_ID])
REFERENCES [CORE].[ROLE] ([ROLE_ID])
GO

ALTER TABLE [CORE].[APP_USER_GLOBAL_ROLE] CHECK CONSTRAINT [FK_AppUserGlobalRole_Role]
GO

ALTER TABLE [CORE].[APP_USER_PREFERENCES] ADD  CONSTRAINT [DF_APP_USER_PREFERENCES_Ver]  DEFAULT ((0)) FOR [Ver]
GO

ALTER TABLE [CORE].[APP_USER_PREFERENCES]  WITH NOCHECK ADD  CONSTRAINT [FK_AppUserPref_AppUser] FOREIGN KEY([APP_USER_ID])
REFERENCES [CORE].[APP_USER] ([APP_USER_ID])
GO

ALTER TABLE [CORE].[APP_USER_PREFERENCES] CHECK CONSTRAINT [FK_AppUserPref_AppUser]
GO

ALTER TABLE [CORE].[ROLE] ADD  DEFAULT ('N') FOR [USED_AS_PERMISSION_IND]
GO

ALTER TABLE [CORE].[ROLE_PERMISSION]  WITH CHECK ADD  CONSTRAINT [FK_RolePermission_Permission] FOREIGN KEY([PERMISSION_ID])
REFERENCES [CORE].[PERMISSION] ([PERMISSION_ID])
GO

ALTER TABLE [CORE].[ROLE_PERMISSION] CHECK CONSTRAINT [FK_RolePermission_Permission]
GO

ALTER TABLE [CORE].[ROLE_PERMISSION]  WITH CHECK ADD  CONSTRAINT [FK_RolePermission_Role] FOREIGN KEY([ROLE_ID])
REFERENCES [CORE].[ROLE] ([ROLE_ID])
GO

ALTER TABLE [CORE].[ROLE_PERMISSION] CHECK CONSTRAINT [FK_RolePermission_Role]
GO

ALTER TABLE [CORE].[STD_TEMPLATE] ADD  CONSTRAINT [DF_PCM_STD_TEMPLATE_Ver]  DEFAULT ((0)) FOR [Ver]
GO

---------------------------------------------- Indexes

CREATE UNIQUE NONCLUSTERED INDEX UQ_VoterId ON [dbo].[Voter] (VoterId);
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_PrecinctCode ON [dbo].Precinct (Code);
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_PartyCode ON [dbo].Party (Code);
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_GenderName ON [dbo].Gender (Name);
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

