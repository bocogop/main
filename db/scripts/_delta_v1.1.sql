/*
	drop table [dbo].[AppUserPrecinct]
*/

CREATE TABLE [dbo].[AppUserPrecinct](
	[Id] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[AppUserFK] [numeric](18, 0) NOT NULL,
	[PrecinctFK] int NOT NULL,
	[PrimaryPrecinctInd] char(1) not null,
	[CreatedBy] [varchar](30) NOT NULL,
	[CreatedDate] [datetime] NOT NULL,
	[ModifiedBy] [varchar](30) NOT NULL,
	[ModifiedDate] [datetime] NOT NULL,
 CONSTRAINT [PK_AppUserPrecinct] PRIMARY KEY CLUSTERED ([Id] ASC)
 WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)
)
GO

----------------------------------------- Constraints

ALTER TABLE [dbo].[AppUserPrecinct]  WITH CHECK ADD  CONSTRAINT [FK_AppUserPrecinct_AppUser] FOREIGN KEY([AppUserFK])
REFERENCES [CORE].[AppUser] ([Id])
GO

ALTER TABLE [dbo].[AppUserPrecinct] CHECK CONSTRAINT [FK_AppUserPrecinct_AppUser]
GO

ALTER TABLE [dbo].[AppUserPrecinct]  WITH CHECK ADD  CONSTRAINT [FK_AppUserPrecinct_Precinct] FOREIGN KEY([PrecinctFK])
REFERENCES [dbo].[Precinct] ([Id])
GO

ALTER TABLE [dbo].[AppUserPrecinct] CHECK CONSTRAINT [FK_AppUserPrecinct_Precinct]
GO

----------------------------------------- Indexes

CREATE UNIQUE NONCLUSTERED INDEX UQ_AppUserPrecinct ON [dbo].[AppUserPrecinct] (AppUserFK, PrecinctFK);
GO

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