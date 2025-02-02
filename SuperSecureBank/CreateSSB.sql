USE [SuperSecureBank]
GO
/****** Object:  Table [dbo].[Users]    Script Date: 04/15/2009 13:39:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[Users](
	[UserID] [int] NULL,
	[Username] [varchar](50) NULL,
	[Password] [varchar](50) NULL,
	[email] [varchar](255) NULL,
	[Type] [varchar](50) NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[Session]    Script Date: 04/15/2009 13:39:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Session](
	[SessionID] [int] NULL,
	[UserID] [int] NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Accounts]    Script Date: 04/15/2009 13:39:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[Accounts](
	[AccountID] [int] IDENTITY(1,1) NOT NULL,
	[UserID] [int] NULL,
	[Type] [varchar](50) NULL,
	[Class] [varchar](50) NULL,
	[Amount] [int] NULL,
	[Activity] [varchar](50) NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO

INSERT INTO [dbo].[Accounts]
	VALUES (1,	'Checking',	'Gold',	150,	'Active');
GO
INSERT INTO [dbo].[Accounts]
	VALUES (1,	'Savings',	'Silver',	200,	'Active');
GO
INSERT INTO [dbo].[Accounts]
	VALUES (1,	'SmallLoan',	'Gold',	-5000,	'Active');
GO
INSERT INTO [dbo].[Accounts]
	VALUES (1,	'Checking',	'Gold',	1500,	'Active');
GO
INSERT INTO [dbo].[Accounts]
	VALUES (1,	'Checking',	'Silver',	200,	'Active');
GO
INSERT INTO [dbo].[Accounts]
	VALUES (1,	'Checking',	'Silver',	500,	'Active');
GO
INSERT INTO [dbo].[Accounts]
	VALUES (1,	'Credit',	'Silver',	-4,	'Active');
GO

INSERT INTO [dbo].[Users]
	VALUES (1, 'joe'	,'letmein'	,'joe@joe.com'	,'Admin');
GO
INSERT INTO [dbo].[Users]
	VALUES (2, 'jim'	,'123456'	,'jim@joe.com'	,'User');
GO
INSERT INTO [dbo].[Users]
	VALUES (3, 'jack'	,'1234'	,'coolguy1341@hotmail.com'	,'User');
GO