package com.mindex.challenge;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChallengeApplicationTests {

	private String reportingStructureUrl;
	private String compensationUrl;
	private String compensationIdUrl;
	private String employeeUrl;

	@Autowired
	private ReportingStructureService reportingStructureService;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Before
	public void setup() {
		employeeUrl = "http://localhost:" + port + "/employee";
		reportingStructureUrl = "http://localhost:" + port + "/reportingstructure/{id}";
		compensationUrl = "http://localhost:" + port + "/compensation";
		compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
	}

	@Test
	public void contextLoads() {
		/*
		 * First, a set of Task1 tests.
		 *
		 * ReportingStructure is fairly simple with no persistence, so just test
		 * specific calls with known values.
		 */

		//case1: invalid employeeId:
		String testEmployeeId = "FakeVal";

		ReportingStructure readReports = restTemplate.getForEntity(reportingStructureUrl,
						ReportingStructure.class,
						testEmployeeId)
						.getBody();

		assert(readReports == null || readReports.getEmployee() == null);

		//case2: verify known values
		String[] testEmployeeIDs = {"16a596ae-edd3-4847-99fe-c4518e82c86f",
									"b7839309-3348-463b-a7e3-5de1c168beb3",
									"03aa1462-ffa9-4978-901b-7c001562cf6f"};

		int[] correctResults = {4,0,2};

		for(int i = 0; i < 3; i++) {
			readReports = restTemplate.getForEntity(reportingStructureUrl,
							ReportingStructure.class,
							testEmployeeIDs[i])
							.getBody();
			assertEquals(readReports.getNumberOfReports(), correctResults[i]);
		}

		/*
		 * Now for Task2 tests.
		 * Similar to employee tests, make some additional Compensations
		 * and verify the info is correct
		 */

		//case3: Post Compensations
		Employee testEmployee = new Employee();
		testEmployee.setFirstName("John");
		testEmployee.setLastName("Doe");
		testEmployee.setDepartment("Engineering");
		testEmployee.setPosition("Developer");
		testEmployee.setEmployeeId("FakeID");

		Compensation testCompensation = new Compensation();
		testCompensation.setEmployee(testEmployee);
		testCompensation.setSalary(30000);
		testCompensation.setEffectiveDate("04/05/2020");

		//This post fails as no employee currently matches
		Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();
		assert(createdCompensation == null || createdCompensation.getEmployee() == null);

		//push the test employee and try again
		Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();
		testCompensation.setEmployee(createdEmployee);
		createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();
		assertCompensationEquivalence(testCompensation, createdCompensation);

		//case4: Read Compensations
		Compensation readCompensation = restTemplate.getForEntity(compensationIdUrl, Compensation.class, testCompensation.getEmployee().getEmployeeId()).getBody();
		assertCompensationEquivalence(testCompensation, readCompensation);
	}

	private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
		assertEquals(expected.getEmployee().getFirstName(), actual.getEmployee().getFirstName());
		assertEquals(expected.getEmployee().getLastName(), actual.getEmployee().getLastName());
		assertEquals(expected.getEmployee().getDepartment(), actual.getEmployee().getDepartment());
		assertEquals(expected.getEmployee().getPosition(), actual.getEmployee().getPosition());

		assertEquals(expected.getSalary(), actual.getSalary());
		assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
	}

}
