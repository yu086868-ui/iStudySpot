```
[060](https://github.com/yu086868-ui/iStudySpot/actions/runs/26405773721/job/77728667583?pr=96#step:4:4061)        Error:  Failures:    

​            Error:    AIControllerTest.testChatWithEmptyCharacterId:94 expected: <400> but was: <500>   

​            Error:    AIControllerTest.testChatWithEmptySessionId:80 expected: <400> but was: <500>   

​            Error:    AIControllerTest.testChatWithIllegalArgumentException:125 expected: <400> but was: <500>   

​            Error:    AIControllerTest.testStreamChatWithEmptySessionId:173 expected: not <null>   

​            Error:    AIServiceImplTest.testChatWithInvalidCharacter:69 Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.   

​            Error:    AIServiceImplTest.testGetCharacter:47 expected: <null> but was: <com.ycyu.istudyspotbackend.entity.Character@599fab92>   

​            Error:    AIServiceImplTest.testGetCharacters:37 expected: <4> but was: <7>   

​            Error:    DeepSeekServiceImplTest.testStreamChat:183 expected: <true> but was: <false>   

​            Error:    OrderServiceImplTest.testCheckinOrderStatusIncorrect:245 expected: <订单状态不正确，无法签到> but was: <订单状态不正确，无法签到，当前状态：pending>   

​            Error:    OrderServiceImplTest.testRenewEndTimeBeforeOriginal:381 expected: <新结束时间必须晚于原结束时间> but was: <订单未在使用中>   

​            Error:  Errors:    

​            Error:    CheckInControllerTest.testGetCurrentCheckInStatus:83 » Servlet Request processing failed: java.lang.NullPointerException: Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findCurrentCheckinByUserId(java.lang.Long)" because "this.orderMapper" is null   

​            Error:    OrderServiceImplTest.testCheckin:206 » Runtime 订单状态不正确，无法签到，当前状态：2   

​            Error:    OrderServiceImplTest.testRenew:324 » Runtime 订单未在使用中   

​            Error:    SeatServiceImplTest.testGetSeatDetail:114 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null   

​            Error:    SeatServiceImplTest.testGetSeatList:59 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null   

​            Error:    SeatServiceImplTest.testGetSeatListWithEmptySeats:88 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null   

​            Error:    SeatServiceImplTest.testGetSeatListWithNullParameters:104 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null   

​            Error:    SeatServiceImplTest.testGetSeatMap:155 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null   

​            Error:    SeatServiceImplTest.testGetSeatMapWithEmptySeats:183 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null   

​            Error:    SeatServiceImplTest.testGetSeatMapWithNullRowCol:207 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null   

​            Error:    JwtUtilsTest.testGenerateRefreshToken:21 » NullPointer Cannot invoke "String.getBytes()" because "this.secretKey" is null   

​            Error:    JwtUtilsTest.testGenerateToken:13 » NullPointer Cannot invoke "String.getBytes()" because "this.secretKey" is null   

​            Error:    JwtUtilsTest.testGetUserIdFromToken:30 » NullPointer Cannot invoke "String.getBytes()" because "this.secretKey" is null   

​            Error:    JwtUtilsTest.testValidateToken:38 » NullPointer Cannot invoke "String.getBytes()" because "this.secretKey" is null   

​            [INFO]    

​            Error:  Tests run: 224, Failures: 10, Errors: 14, Skipped: 0   

​            [INFO]    

​            [INFO] ------------------------------------------------------------------------   

​            [INFO] BUILD FAILURE   

​            [INFO] ------------------------------------------------------------------------   

​            [INFO] Total time:  45.106 s   

​            [INFO] Finished at: 2026-05-25T14:35:35Z   

​            [INFO] ------------------------------------------------------------------------   

​            Error:  Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.1.2:test (default-test) on project istudyspot-backend: There are test failures.   

​            Error:     

​            Error:  Please refer to /home/runner/work/iStudySpot/iStudySpot/backend/istudyspot-backend/target/surefire-reports for the individual test results.   

​            Error:  Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.   

​            Error:  -> [Help 1]   

​            Error:     

​            Error:  To see the full stack trace of the errors, re-run Maven with the -e switch.   

​            Error:  Re-run Maven using the -X switch to enable full debug logging.   

​            Error:     

​            Error:  For more information about the errors and possible solutions, please read the following articles:   

​            Error:  [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException   

​            Error: Process completed with exit code 1.   
```



```
57
[INFO] 
[INFO] Results:
[INFO] 
Error:  Failures: 
Error:    AIControllerTest.testChatWithEmptyCharacterId:94 expected: <400> but was: <500>
Error:    AIControllerTest.testChatWithEmptySessionId:80 expected: <400> but was: <500>
Error:    AIControllerTest.testChatWithIllegalArgumentException:125 expected: <400> but was: <500>
Error:    AIControllerTest.testStreamChatWithEmptySessionId:173 expected: not <null>
Error:    AIServiceImplTest.testChatWithInvalidCharacter:69 Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.
Error:    AIServiceImplTest.testGetCharacter:47 expected: <null> but was: <com.ycyu.istudyspotbackend.entity.Character@599fab92>
Error:    AIServiceImplTest.testGetCharacters:37 expected: <4> but was: <7>
Error:    DeepSeekServiceImplTest.testStreamChat:183 expected: <true> but was: <false>
Error:    OrderServiceImplTest.testCheckinOrderStatusIncorrect:245 expected: <订单状态不正确，无法签到> but was: <订单状态不正确，无法签到，当前状态：pending>
Error:    OrderServiceImplTest.testRenewEndTimeBeforeOriginal:381 expected: <新结束时间必须晚于原结束时间> but was: <订单未在使用中>
Error:  Errors: 
Error:    CheckInControllerTest.testGetCurrentCheckInStatus:83 » Servlet Request processing failed: java.lang.NullPointerException: Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findCurrentCheckinByUserId(java.lang.Long)" because "this.orderMapper" is null
Error:    OrderServiceImplTest.testCheckin:206 » Runtime 订单状态不正确，无法签到，当前状态：2
Error:    OrderServiceImplTest.testRenew:324 » Runtime 订单未在使用中
Error:    SeatServiceImplTest.testGetSeatDetail:114 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null
Error:    SeatServiceImplTest.testGetSeatList:59 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null
Error:    SeatServiceImplTest.testGetSeatListWithEmptySeats:88 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null
Error:    SeatServiceImplTest.testGetSeatListWithNullParameters:104 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null
Error:    SeatServiceImplTest.testGetSeatMap:155 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null
Error:    SeatServiceImplTest.testGetSeatMapWithEmptySeats:183 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null
Error:    SeatServiceImplTest.testGetSeatMapWithNullRowCol:207 » NullPointer Cannot invoke "com.ycyu.istudyspotbackend.mapper.OrderMapper.findActiveByRoomId(java.lang.Long)" because "this.orderMapper" is null
Error:    JwtUtilsTest.testGenerateRefreshToken:21 » NullPointer Cannot invoke "String.getBytes()" because "this.secretKey" is null
Error:    JwtUtilsTest.testGenerateToken:13 » NullPointer Cannot invoke "String.getBytes()" because "this.secretKey" is null
Error:    JwtUtilsTest.testGetUserIdFromToken:30 » NullPointer Cannot invoke "String.getBytes()" because "this.secretKey" is null
Error:    JwtUtilsTest.testValidateToken:38 » NullPointer Cannot invoke "String.getBytes()" because "this.secretKey" is null
[INFO] 
Error:  Tests run: 224, Failures: 10, Errors: 14, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  45.106 s
[INFO] Finished at: 2026-05-25T14:35:35Z
[INFO] ------------------------------------------------------------------------
Error:  Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.1.2:test (default-test) on project istudyspot-backend: There are test failures.
Error:  
Error:  Please refer to /home/runner/work/iStudySpot/iStudySpot/backend/istudyspot-backend/target/surefire-reports for the individual test results.
Error:  Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.
Error:  -> [Help 1]
Error:  
Error:  To see the full stack trace of the errors, re-run Maven with the -e switch.
Error:  Re-run Maven using the -X switch to enable full debug logging.
Error:  
Error:  For more information about the errors and possible solutions, please read the following articles:
Error:  [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
Error: Process completed with exit code 1.

```

