For /F "tokens=*" %%A in ('CD') Do Set cd=%%A
call %cd%\..\cidsLib\ext\setClassPath.cmd %cd%\..\cidsLib\ext\
java  -Xms128m -Xmx512m -Djava.security.policy=config\cids.policy.file -cp ..\cidsLib\cidsMetaJdbc.jar;..\cidsLib\cidsServer.jar;..\cidsLib\cidsUtils.jar;%cidsExtClassPath%  Sirius.server.middleware.impls.proxy.RemoteTester %1