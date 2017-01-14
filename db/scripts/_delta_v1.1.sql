create function [dbo].[AgeAtDate](
    @DOB    datetime,
    @PassedDate datetime
)
returns int
with SCHEMABINDING
as
begin

declare @iMonthDayDob int
declare @iMonthDayPassedDate int

select @iMonthDayDob = CAST(datepart (mm,@DOB) * 100 + datepart  (dd,@DOB) AS int) 
select @iMonthDayPassedDate = CAST(datepart (mm,@PassedDate) * 100 + datepart  (dd,@PassedDate) AS int) 

return DateDiff(yy,@DOB, @PassedDate) 
- CASE WHEN @iMonthDayDob <= @iMonthDayPassedDate
  THEN 0 
  ELSE 1
  END
End
GO

alter table Voter add AgeApprox as dbo.AgeAtDate(DATEFROMPARTS(BirthYear, 7, 1), getdate())
GO
alter table Voter_H add AgeApprox int
GO

ALTER TRIGGER dbo.[TR_Voter_INS_H] ON dbo.Voter
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
			,[Ver]
			,[AgeApprox])
SELECT 'I',SYSUTCDATETIME()
           ,i.*
  FROM inserted i
END
GO

--
ALTER TRIGGER dbo.TR_Voter_UPD_H ON dbo.Voter
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
			,[Ver]
			,[AgeApprox])
SELECT 'U',SYSUTCDATETIME(), i.*
  FROM inserted i
END
GO


--
ALTER TRIGGER dbo.TR_Voter_DEL_H ON dbo.Voter
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
			,[Ver]
			,[AgeApprox])
SELECT 'D',SYSUTCDATETIME(), i.*
  FROM deleted i
END
GO

----------------------------------------- Constraints


----------------------------------------- Indexes

----------------------------------------- Data

INSERT INTO [CORE].[AppUser]
           ([Username]
           ,[PasswordHash]
           ,[TimeZone]
           ,[FirstName]
           ,[MiddleName]
           ,[LastName]
           ,[Description]
           ,[Phone]
           ,[Email]
           ,[EnabledInd]
           ,[CreatedBy]
           ,[CreatedDate]
           ,[ModifiedBy]
           ,[ModifiedDate]
           ,[Ver])
     VALUES
           ('trekmbikes'
		   -- summer
           ,'$2a$10$LXBQJ13OYWUGfN4orrOgdOB4p/K9etDR.v1iKUASVL64ZKB8SN8jq'
           ,'America/Denver'
           ,'Connor'
           ,''
           ,'Barry'
           ,''
           ,'720-258-6545'
           ,'connor@slickapps.com'
           ,'Y'
           ,'Initial Load'
           ,SYSUTCDATETIME()
           ,'Initial Load'
           ,SYSUTCDATETIME()
           ,1)
GO

INSERT INTO [CORE].[AppUserGlobalRole]
           ([AppUserFK]
           ,[RoleFK]
           ,[CreatedBy]
           ,[CreatedDate]
           ,[ModifiedBy]
           ,[ModifiedDate])
     select a.id, 
           r.id
           ,'Initial Load'
           ,SYSUTCDATETIME()
           ,'Initial Load'
           ,SYSUTCDATETIME()
		from Core.AppUser a,
		Core.Role r
		where a.username = 'trekmbikes'
		and r.Name <> 'Voter'
GO

/*
ALTER TABLE [dbo].[Voter]
	add NameLastUpdated datetime,
	PhoneLastUpdated datetime,
	AddressLastUpdated datetime,
	PartyAffiliationLastUpdated datetime,
	GenderLastUpdated datetime,

*/

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.default', cast('Sorry, the logon credentials were invalid.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);
insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.expired', cast('Sorry, your account is expired. Please contact Boulder County GOP staff.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);
insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.disabled', cast('Sorry, your account is disabled. Please contact Boulder County GOP staff.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);
insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('login.error.locked', cast('Sorry, your account is locked. Please contact Voluntary Service staff or wait 15 minutes for your account to unlock.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('homepageContent', cast('Welcome to the Boulder County GOP Staff administration tool.' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);
insert into Core.Template(TemplateName, TemplateBody, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)
values('homepageAnnouncement', cast('' as varbinary(max)), 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME(), 1);

