-- The following can be run at any time to overlay phone and email onto the Voter table - CPB

UPDATE dbo.Voter v
	INNER JOIN tbl_CE068_20161116 n ON v.VoterId  = n.VOTER_ID 
SET v.Phone = n.PHONE
WHERE n.PHONE IS NOT NULL
	AND v.Phone IS NULL;

UPDATE dbo.Voter v
	INNER JOIN tbl_20160330_REP_Caucus rc ON v.VoterId = rc.VoterID
SET v.Email = rc.Email
WHERE rc.Email IS NOT NULL
	AND v.Email IS NULL;