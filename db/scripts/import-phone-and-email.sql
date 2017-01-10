-- The following can be run at any time to overlay phone and email onto the Voter table - CPB

------------------------ Email

UPDATE v
SET v.Email = VI.EMAIL_1
FROM dbo.Voter v
	INNER JOIN tbl_sup_v_info_20140410 VI ON VI.VOTER_ID = v.VoterId
WHERE VI.EMAIL_1 IS NOT NULL
	AND VI.EMAIL_1 <> 'Send Letter'
	AND v.Email IS NULL;
	
UPDATE v
SET v.Email = rc.Email
FROM dbo.Voter v
	INNER JOIN tbl_20160330_REP_Caucus rc ON v.VoterId = rc.VoterID
WHERE rc.Email IS NOT NULL
	AND v.Email IS NULL;

------------------------ Phone
	
UPDATE v
SET v.Phone = VI.PHONE_1
FROM dbo.Voter v
	INNER JOIN tbl_sup_v_info_20140410 VI ON VI.VOTER_ID = v.VoterId
WHERE VI.PHONE_1 IS NOT NULL
	AND v.Phone IS NULL;
	
UPDATE v
SET v.Phone = V24.HOME_PHONE
FROM dbo.Voter v
	INNER JOIN tbl_VR024_20160203 V24 ON v.VoterId = V24.VOTER_ID
WHERE V24.HOME_PHONE IS NOT NULL
	AND v.Phone IS NULL;