delete from core.RolePermission where RoleFK = 100
GO

CREATE UNIQUE NONCLUSTERED INDEX UQ_RolePermission ON core.[RolePermission] (RoleFK, PermissionFK);
GO

INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 1, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 2, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 3, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 4, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 5, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
INSERT INTO [CORE].[RolePermission]([RoleFK], [PermissionFK], [CreatedBy], [CreatedDate], [ModifiedBy], [ModifiedDate])
     VALUES(100, 6, 'Initial Load', SYSUTCDATETIME(), 'Initial Load', SYSUTCDATETIME());
GO

sp_rename 'CORE.AppUserGlobalRole', 'AppUserRole';
GO