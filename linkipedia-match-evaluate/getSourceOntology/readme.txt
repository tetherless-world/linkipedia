1. "namesForSource" are all the unique names that are in the manual_annotation file
2. The script will take each name and supply it to the eml2owl.py file and save the resulting file.
3. In the script, I am replacing "/" that occurs in the name with "ZZZZ" because it gave errors while creating a file. Hence, all the filenames will have "ZZZZ" instead of "/"