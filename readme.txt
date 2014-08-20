Readme.txt
==========

Use of this Given-When-Then fixture is simple.
Just take the "GivenWhenThenFixture.java" file and add it to your project.
Then subclass it for each of the fixtures you want to use a given/when/then syntax in.

See the javadoc comments in GivenWhenThenFixture.java and the example in:

src/test/resources & 
src/test/java/com/fmr/fitnesse/example.

Supported steps are:
====================

GIVEN
AND* (any number of additional AND steps)
WHEN
THEN
AND* (any number of additional AND steps)

Notes:
======
Step methods you create in your subclass of "GivenWhenThenFixture" should return void.
Step methods for THEN/AND should return a boolean

Examples (Fitnesse Wiki syntax):
================================

-|Table:Test User Login|
|given|user [John Doe] arrives at the login page|
|and|enters password of [BAD_PASSWORD]|
|when|he clicks on the login button|
|then|he should see a result message of [Invalid Username or Password!]|
|and|he is directed to page [loginerror.html]|

-|Table:Test Bank Account|
|given|user [John Doe] has no money in their account|
|when|[100] dollars is deposited in to the account|
|then|the balance should be [100] dollars|
