# Distributed Transactions for Rest Services

## This Example shows you the possibilty to use distributed transacation with 2 Phase Commit in Spring boot Microservices using Atomikos

### Overview of Example

There are three microservices involved
1. OrderManager : Springboot service starts the Transactions and acts as an Orchestrator and Coordinator for the transaction. Saves data in purchase_order table.
2. Payment: Springboot service participates the Global Transaction started by the OrderManager. Saves data in payment table.
3. Inventory: Springboot service participates the Global Transaction started by the OrderManager. Updates data in inventory table.

### Communication Flow 

When the OrderManager Service receives an order to process, 
1. It starts a transaction and creates a root id
2. Calls the Payment service to process the payment , the root id is sent to payment service in request headers.
3. The Payment service starts a composite transaction (sub-transaction) and provides an Extent string back to OrderManger in response headers
4. Calls the Inventory service to process the payment , the root id is sent to inventory service in request headers.
5. The Inventory service starts a composite transaction (sub-transaction) and provides an Extent string back to OrderManger in response headers
6. Once the OrderManager receives success response and extents from the two services , it then calls prepare APIs of the two services (The information of the prepare APIs is found in the Extents)
7. If all Prepare calls succeed, then the OrderManger calls the commit APIs of the two services and the services commit their sub-transactions.
8. If any one of the Prepare fails, then the OrderManger calls the rollback APIs of the two services and the services rollback their sub-transactions.
9. After Commit or Rollback is complete , then the transaction created by the OrderManager is closed.

![communication flow](./overview.png?raw=true "Overview")
