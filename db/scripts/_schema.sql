-- drop tables if needed - CPB
/*
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Voter' AND TABLE_SCHEMA = 'dbo') drop table dbo.Voter;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Gender' AND TABLE_SCHEMA = 'dbo') drop table dbo.Gender;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Party' AND TABLE_SCHEMA = 'dbo') drop table dbo.Party;
if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'Precinct' AND TABLE_SCHEMA = 'dbo') drop table dbo.Precinct;
*/

create table dbo.Precinct(
	[Id] [int] IDENTITY(1,1) NOT NULL,
	Code varchar(20) not null,
	Name varchar(40) not null,
	CONSTRAINT [PK_Precinct] PRIMARY KEY CLUSTERED ([Id] ASC)
)
CREATE UNIQUE NONCLUSTERED INDEX UQ_PrecinctCode ON [dbo].Precinct (Code);
GO
-- Precinct data will be populated on first run of the transform script - CPB

create table dbo.Party(
	[Id] [int] IDENTITY(1,1) NOT NULL,
	Code varchar(3) not null,
	Name varchar(40) not null,
	CONSTRAINT [PK_party] PRIMARY KEY CLUSTERED ([Id] ASC)
)
CREATE UNIQUE NONCLUSTERED INDEX UQ_PartyCode ON [dbo].Party (Code);
GO

/*	initial seed; we only get new codes from the state so we'll just
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

create table dbo.Gender(
	[Id] [int] IDENTITY(1,1) NOT NULL,
	Code varchar(3) not null,
	Name varchar(40) not null,
	CONSTRAINT [PK_Gender] PRIMARY KEY CLUSTERED ([Id] ASC)
)

/*	initial seed; we only get new names from the state so we'll just
	populate the codes as '?' in the transform script and manually
	update the codes later - CPB */
insert into dbo.Gender(Code, Name) values('F', 'Female');
insert into dbo.Gender(Code, Name) values('M', 'Male');
insert into dbo.Gender(Code, Name) values('U', 'Unknown');
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_GenderName ON [dbo].Gender (Name);
GO

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

CREATE UNIQUE NONCLUSTERED INDEX UQ_VoterId ON [dbo].[Voter] (VoterId);
GO

ALTER TABLE [dbo].[Voter] WITH CHECK ADD CONSTRAINT [FK_Voter_Party] FOREIGN KEY (PartyFK) REFERENCES [dbo].Party([Id]);
ALTER TABLE [dbo].[Voter] CHECK CONSTRAINT [FK_Voter_Party];

ALTER TABLE [dbo].[Voter] WITH CHECK ADD CONSTRAINT [FK_Voter_Gender] FOREIGN KEY (GenderFK) REFERENCES [dbo].Gender([Id]);
ALTER TABLE [dbo].[Voter] CHECK CONSTRAINT [FK_Voter_Gender];

ALTER TABLE [dbo].[Voter] WITH CHECK ADD CONSTRAINT [FK_Voter_Precinct] FOREIGN KEY (PrecinctFK) REFERENCES [dbo].Precinct([Id]);
ALTER TABLE [dbo].[Voter] CHECK CONSTRAINT [FK_Voter_Precinct];

