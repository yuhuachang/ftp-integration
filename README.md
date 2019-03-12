# FTP Integration Flow
This project is to use FTP as the integration flow endpoint.
Inbound flow is to read file (Excel file, CSV, XML, and others) from input folder, process, and move it to the archive folder.
If there is any error, move the file to the error folder.

### Result
It takes a lot of time handling socket errors and exceptions of FTP component.
I found it is easier to use Apache Camel.
