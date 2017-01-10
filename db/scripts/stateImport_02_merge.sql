update [dbo].[stg_20170109T2306_VR011_20170109] set VOTER_ID = SUBSTRING(VOTER_ID, 2, LEN(VOTER_ID))
where LEFT(VOTER_ID, 1) = '"'
GO
update [dbo].[stg_20170109T2306_VR011_20170109] set SSN = null
where SSN is not null
	and LTRIM(RTRIM(SSN)) = ''
GO
update [dbo].[stg_20170109T2306_VR011_20170109] set PHONE_NUM = null
where PHONE_NUM is not null
	and LTRIM(RTRIM(PHONE_NUM)) = ''
GO
update [dbo].[stg_20170109T2306_VR011_20170109] set FAX = null
where FAX is not null
	and LTRIM(RTRIM(FAX)) = ''
GO
update [dbo].[stg_20170109T2306_VR011_20170109] set EMAIL = null
where EMAIL is not null
	and LTRIM(RTRIM(EMAIL)) = ''
GO

-- populate any new precincts
insert into dbo.Precinct(Code, Name)
select distinct n.PRECINCT_CODE, n.PRECINCT_NAME
from [dbo].[stg_20170109T2306_VR011_20170109] n
where n.PRECINCT_CODE not in (
	select Code from dbo.Precinct
)
GO

-- populate any new parties
insert into dbo.Party(Code, Name)
select distinct n.PARTY, 'UnknownNewName'
from [dbo].[stg_20170109T2306_VR011_20170109] n
where n.Party not in (
	select Code from dbo.Party
)
GO

-- populate any new genders
insert into dbo.Gender(Code, Name)
select distinct '?', n.GENDER
from [dbo].[stg_20170109T2306_VR011_20170109] n
where n.GENDER not in (
	select Name from dbo.Gender
)
GO

MERGE dbo.Voter AS T  
USING (
	select s.*
		,PartyFK = p.id
		,GenderFK = g.id
		,PrecinctFK = pr.id
	from [dbo].[stg_20170109T2306_VR011_20170109] s
		left join dbo.Party p on s.PARTY = p.Code
		left join dbo.Gender g on s.GENDER = g.Name
		left join dbo.Precinct pr on s.PRECINCT_CODE = pr.Code
	) AS S
ON T.VoterId = S.VOTER_ID
WHEN MATCHED THEN  
  UPDATE SET
	t.[FirstName] = dbo.MixedCase(s.FIRST_NAME)
      ,t.[MiddleName] = dbo.MixedCase(s.MIDDLE_NAME)
      ,t.[LastName] = dbo.MixedCase(s.LAST_NAME)
      ,t.[NameSuffix] = dbo.MixedCase(s.NAME_SUFFIX)
      ,t.[DriversLicense] = s.DRIVERS_LICENSE
      ,t.[SSN] = s.SSN
      ,t.[RegistrationDate] = CONVERT(date, s.REGISTRATION_DATE, 101)
      ,t.[EffectiveDate] = CONVERT(date, s.EFFECTIVE_DATE, 101)
		-- preserve existing phone number since we also populate this elsewhere
      ,t.[Phone] = ISNULL(s.PHONE_NUM, t.Phone)
      ,t.[HouseNumber] = s.HOUSE_NUM
      ,t.[HouseSuffix] = s.HOUSE_SUFFIX
      ,t.[PreDirection] = s.PRE_DIR
      ,t.[StreetName] = dbo.MixedCase(s.STREET_NAME)
      ,t.[StreetType] = s.STREET_TYPE
      ,t.[PostDirection] = s.POST_DIR
      ,t.[UnitType] = s.UNIT_TYPE
      ,t.[UnitNumber] = s.UNIT_NUM
      ,t.[ResidentialAddress] = dbo.MixedCase(s.RESIDENTIAL_ADDRESS)
      ,t.[ResidentialCity] = dbo.MixedCase(s.RESIDENTIAL_CITY)
      ,t.[ResidentialState] = s.RESIDENTIAL_STATE
      ,t.[ResidentialZip] = s.RESIDENTIAL_ZIP_CODE
      ,t.[ResidentialZipPlus] = s.RESIDENTIAL_ZIP_PLUS
      ,t.[MailingAddress1] = dbo.MixedCase(s.MAILING_ADDRESS_1)
      ,t.[MailingAddress2] = dbo.MixedCase(s.MAILING_ADDRESS_2)
      ,t.[MailingAddress3] = dbo.MixedCase(s.MAILING_ADDRESS_3)
      ,t.[MailingCity] = dbo.MixedCase(s.MAILING_CITY)
      ,t.[MailingState] = s.MAILING_STATE
      ,t.[MailingZip] = s.MAILING_ZIP_CODE
      ,t.[MailingZipPlus] = s.MAILING_ZIP_PLUS
      ,t.[MailingCountry] = dbo.MixedCase(s.MAILING_COUNTRY)
      ,t.[BallotAddress1] = dbo.MixedCase(s.BALLOT_ADDRESS1)
      ,t.[BallotAddress2] = dbo.MixedCase(s.BALLOT_ADDRESS2)
      ,t.[BallotAddress3] = dbo.MixedCase(s.BALLOT_ADDRESS3)
      ,t.[BallotCity] = dbo.MixedCase(s.BALLOT_CITY)
      ,t.[BallotState] = s.BALLOT_STATE
      ,t.[BallotZip] = s.BALLOT_ZIP_CODE
      ,t.[BallotZipPlus] = s.BALLOT_ZIP_PLUS
      ,t.[BallotCountry] = dbo.MixedCase(s.BALLOT_COUNTRY)
      ,t.[VoterStatusActive] = case when s.VOTER_STATUS = 'Active' then 1 else 0 end
      ,t.[VoterStatusReason] = s.STATUS_REASON
      ,t.[PartyFK] = s.PartyFK
      ,t.[AffiliatedDate] = CONVERT(date, s.AFF_DATE, 101)
      ,t.[GenderFK] = s.GenderFK
      ,t.[IdRequired] = case when s.ID_REQUIRED = 'Yes' then 1 else 0 end
      ,t.[BirthYear] = s.BIRTH_YEAR
      ,t.[PrecinctFK] = s.PrecinctFK
      ,t.[UOCAVA] = case when s.UOCAVA = 'Yes' then 1 else 0 end
      ,t.[IssueMethod] = s.ISSUE_METHOD
      ,t.[Fax] = s.FAX
	  -- preserve existing email since we also populate this elsewhere
      ,t.[Email] = lower(ISNULL(s.EMAIL, t.Email))
	  ,t.ModifiedBy = 'AutoImport'
	  ,t.ModifiedDate = SYSUTCDATETIME()
	  ,t.Ver = t.Ver + 1
WHEN NOT MATCHED BY TARGET THEN  
  INSERT ([VoterId]
           ,[FirstName]
           ,[MiddleName]
           ,[LastName]
           ,[NameSuffix]
		   ,[Nickname]
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
	 VALUES (
		s.VOTER_ID
		,dbo.MixedCase(s.FIRST_NAME)
      ,dbo.MixedCase(s.MIDDLE_NAME)
      ,dbo.MixedCase(s.LAST_NAME)
      ,dbo.MixedCase(s.NAME_SUFFIX)
	  ,dbo.MixedCase(s.FIRST_NAME)
      ,s.DRIVERS_LICENSE
      ,s.SSN
      ,CONVERT(date, s.REGISTRATION_DATE, 101)
      ,CONVERT(date, s.EFFECTIVE_DATE, 101)
      ,s.PHONE_NUM
	  ,null
      ,s.HOUSE_NUM
      ,s.HOUSE_SUFFIX
      ,s.PRE_DIR
      ,dbo.MixedCase(s.STREET_NAME)
      ,s.STREET_TYPE
      ,s.POST_DIR
      ,s.UNIT_TYPE
      ,s.UNIT_NUM
      ,dbo.MixedCase(s.RESIDENTIAL_ADDRESS)
      ,dbo.MixedCase(s.RESIDENTIAL_CITY)
      ,s.RESIDENTIAL_STATE
      ,s.RESIDENTIAL_ZIP_CODE
      ,s.RESIDENTIAL_ZIP_PLUS
      ,dbo.MixedCase(s.MAILING_ADDRESS_1)
      ,dbo.MixedCase(s.MAILING_ADDRESS_2)
      ,dbo.MixedCase(s.MAILING_ADDRESS_3)
      ,dbo.MixedCase(s.MAILING_CITY)
      ,s.MAILING_STATE
      ,s.MAILING_ZIP_CODE
      ,s.MAILING_ZIP_PLUS
      ,dbo.MixedCase(s.MAILING_COUNTRY)
      ,dbo.MixedCase(s.BALLOT_ADDRESS1)
      ,dbo.MixedCase(s.BALLOT_ADDRESS2)
      ,dbo.MixedCase(s.BALLOT_ADDRESS3)
      ,dbo.MixedCase(s.BALLOT_CITY)
      ,s.BALLOT_STATE
      ,s.BALLOT_ZIP_CODE
      ,s.BALLOT_ZIP_PLUS
      ,dbo.MixedCase(s.BALLOT_COUNTRY)
      ,case when s.VOTER_STATUS = 'Active' then 1 else 0 end
      ,s.STATUS_REASON
      ,s.PartyFK
      ,CONVERT(date, s.AFF_DATE, 101)
      ,s.GenderFK
      ,case when s.ID_REQUIRED = 'Yes' then 1 else 0 end
      ,s.BIRTH_YEAR
      ,s.PrecinctFK
      ,case when s.UOCAVA = 'Yes' then 1 else 0 end
      ,s.ISSUE_METHOD
      ,s.FAX
      ,lower(s.EMAIL)
	  ,null
	  ,'AutoImport'
	  ,SYSUTCDATETIME()
	  ,'AutoImport'
	  ,SYSUTCDATETIME()
	  ,1)
WHEN NOT MATCHED BY SOURCE THEN
	UPDATE SET
	  t.[VoterStatusActive] = 0
      ,t.[VoterStatusReason] = 'RemovedByState'
	  ,t.ModifiedBy = 'AutoImport'
	  ,t.ModifiedDate = SYSUTCDATETIME()
	  ,t.Ver = t.Ver + 1
;
GO