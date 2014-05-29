Peer-and-Group-Evaluation-in-Classroom
======================================

In normal classroom interaction there will be a teacher who monitors and organizes the activities. The teacher will divide the students into the groups and organizes the quizes to test their understanding of the material which was taught in the class. But, this organization of quizes to manage and to monitor each individual's performance in the class tends to be harder. To resolve this problem we are presenting a new solution of evaluating the students performance which will also provide a complete report of them automatically to the teacher.

1. Teacher will input all the quiz parameters such as: Number of groups, size of each group and also the number of students participating in the quiz.

2. Students who log in and wish to start the quiz will get the guidelines that the teacher inserted and they will select whether they wish to become a leader or not. This process will be time-bounded.

3. Students tend to form groups so, it can be fairly assumed that leaders will be pre-decided by the students. 

4. In that way, the pre-decided leaders will simply opt to become leader.

5. Teacher(In this case, the server) will receive all the requests and store them in a list. The teacher has the option of letting the server choose the leaders based on an algorithm (say randomized or based on previous performance) or she can opt to select the leaders manually. 

6. The leaders will be selected and broadcasted to each client.

7. Individual students will select the leader (in turn, their group). The server will again broadcast when a group gets totally full. This will let students know which groups they can be a part of. 

8. Based on the number of groups, those many queues/lists will be dynamically formed by the server. Each list/queue denotes a group. Also, the maximum size of the group will decide the number of elements that can be inserted into the list/queue.

9. When all the students get assigned to a group (the condition can be checked in the following way: the server knows the total number of students. It will count the total number of elements in all the queues. If they match, all the students have been assigned to a group), teacher will start the quiz.

10. Starting the quiz means that the first group (the queue that is created first) will get the chance to ask a question to all the groups. The questions maybe pre-decided by the group member or can be thought of on-the-spot. 

11. Only the leader has the interface to post the question that will go to the teacher for authentication. The teacher will provide a level of question also. Based on that, the question will carry variable marks. The leader has to provide the question, the answer (choice number) and options (and OPTIONALLY the roll number of the student who initiated the idea of the question). The entire block is sent to the teacher. The teacher authenticates (after searching / or from knowledge) and passes the question to all the other groups. 

12. In case the question is wrong, the teacher rejects the question and it is dropped. Also, the group leader gets a notification that the question has been dropped. The control can be sent to the next group or to the same group for another attempt.

13. The groups will get a chance to post a question in a round-robin fashiion. The number of rounds set by the teacher intitially can be set as a limit. Thus, a simple up-counter modulus (number of groups) will let the server regulate the process of conducting quiz.

14. The students will receive the questions (except the group that sent the question) and they will answer. The answer will be checked locally in the tablets. If the answer is correct, that particular client can send a message to the server stating that it has answered the question correctly and one mark should be alloted against his record. In case, the answer is wrong, a packet can be sent. 

15. The mark alloting packet can contain a flag variable. If the answer (which is checked locally) is correct, then the flag will be 1 and if the answer is incorrect then the flag value will be 0. If the flag is 1, then the server will allot marks against the particular students. If the flag is 0, then no marks will be allotted. 

16. The sending of packet to the server will serve another purpose. The server will come to know when to provide the questioning interface to the next group leader. For answering, each client will get an interface either to answer or not to answer. If he selects not to answer the question, total questions attempted by him will increase (equivalent to wrong answer).

17. After all the questions are asked, the teacher (the server) can close the session and send the performance to each student. 




Databases: (Implemented in the server)

1. student_information 
Fields: roll_number (primary key), password, overall_marks

Description: 
* roll_number is the primary key and the Roll Number of a student. It will be like a username.
* password is for authenticating a student. Initially, it will be provided by the teacher and that can be changed later by the student.
* overall_marks is the marks obtained by the student. Each question is not 1 marks as level is assigned by the teacher. A marking scheme will be provided to the system and according to the level of question, the question will carry that much marks.


2. student_report
Fields: {roll_number, subject, date}(primary key), question_attempted, correct_answers

Description:
* roll_number is the Roll Number of a student as explained above.
* subject refers to the Subject of a particular quiz session.
* date is the Date of the quiz for which the report has been made.
* All 3 together (roll_number, subject, date) form the primary key for this table.
* question_attempted is the number of questions that a particular student received in the particular quiz session.
* correct_answer refers to the number of question that the particular student answered correctly.

3. question_bank
Field: question, answer, subject, roll_number, level

Description:
* question refers to an individual question asked by a group leader AND authenticated by the teacher.
* answer is the above 'question's' answer.
* subject relates to the subject of the question.
* roll_number refers to the roll number of the student whose ideas was this question. (OPTIONAL)
* level refers to the quality of question (ACCORDING TO THE TEACHER) and based on this value, a question will carry a particular value of marks.



