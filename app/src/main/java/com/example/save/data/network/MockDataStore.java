package com.example.save.data.network;

public class MockDataStore {

    public static final String LOGIN_SUCCESS = "{\n" +
            "  \"token\": \"mock_jwt_token_12345\",\n" +
            "  \"user\": {\n" +
            "    \"id\": \"admin_1\",\n" +
            "    \"name\": \"Admin User\",\n" +
            "    \"email\": \"admin@saveapp.com\",\n" +
            "    \"role\": \"admin\",\n" +
            "    \"group_name\": \"Save Together Group\"\n" +
            "  }\n" +
            "}";

    public static final String DASHBOARD_DATA = "{\n" +
            "  \"total_savings\": 1250000.50,\n" +
            "  \"active_loans\": 450000.00,\n" +
            "  \"interest_earned\": 12500.25,\n" +
            "  \"total_members\": 24,\n" +
            "  \"pending_approvals\": 3,\n" +
            "  \"recent_transactions\": [\n" +
            "    {\"id\": \"t1\", \"type\": \"contribution\", \"amount\": 5000, \"member_name\": \"John Doe\", \"date\": \"2024-03-29\"},\n" +
            "    {\"id\": \"t2\", \"type\": \"loan_repayment\", \"amount\": 12000, \"member_name\": \"Jane Smith\", \"date\": \"2024-03-28\"}\n" +
            "  ]\n" +
            "}";

    public static final String MEMBERS_LIST = "[\n" +
            "  {\"id\": \"m1\", \"name\": \"John Doe\", \"email\": \"john@example.com\", \"phone\": \"0711223344\", \"savings\": 45000, \"loan_balance\": 0},\n" +
            "  {\"id\": \"m2\", \"name\": \"Jane Smith\", \"email\": \"jane@example.com\", \"phone\": \"0722334455\", \"savings\": 120000, \"loan_balance\": 15000},\n" +
            "  {\"id\": \"m3\", \"name\": \"Alice Brown\", \"email\": \"alice@example.com\", \"phone\": \"0733445566\", \"savings\": 3000, \"loan_balance\": 0}\n" +
            "]";

    public static final String LOANS_LIST = "[\n" +
            "  {\"id\": \"l1\", \"member_name\": \"Jane Smith\", \"amount\": 20000, \"status\": \"approved\", \"repaid_amount\": 5000},\n" +
            "  {\"id\": \"l2\", \"member_name\": \"Bob Johnson\", \"amount\": 50000, \"status\": \"pending\", \"repaid_amount\": 0}\n" +
            "]";

    public static final String SYSTEM_CONFIG = "{\n" +
            "  \"group_name\": \"Save Together\",\n" +
            "  \"currency\": \"KES\",\n" +
            "  \"interest_rate\": 10,\n" +
            "  \"max_loan_duration_months\": 12\n" +
            "}";

    public static final String API_SUCCESS = "{\"status\": \"success\", \"message\": \"Operation completed successfully (Mock Mode)\"}";
}
