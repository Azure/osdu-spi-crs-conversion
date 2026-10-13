Apache SIS setup

Add the SIS_DATA directory to your local machine and create the env variable SIS_DATA that points to this directory.

Add in additional mapping files to SIS_DATA/DatumChanges directory

When the Apache SIS code runs for the first time, it will populate the SIS_DATA directory with the necessary files, note that this process can take several seconds.

Note that if additional mapping files are added to SIS_DATA/DatumChanges directory, then the SIS_DATA\Databases\SpatialMetadata directory needs to be deleted if it is present.
