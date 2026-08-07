#include <iostream>
#include <fstream>
#include <string>
#include <cstdlib>  // Added for rand()
#include <ctime>    // Added for time()
using namespace std;

// ==================== DATA STRUCTURES ====================

// 1. ARRAY - Fixed size menu
struct MenuItem {
    int id;
    string name;
    int price;
    int stock;
};

// Menu array with 10 items
MenuItem menu[10] = {
    {1, "Burger", 200, 50},
    {2, "Pizza", 500, 30},
    {3, "Fries", 150, 100},
    {4, "Sandwich", 180, 40},
    {5, "Pasta", 350, 25},
    {6, "Nuggets", 250, 60},
    {7, "Shawarma", 300, 35},
    {8, "Cold Drink", 100, 120},
    {9, "Coffee", 120, 80},
    {10, "Ice Cream", 160, 45}
};

// 2. LINKED LIST for menu display
struct Node {
    MenuItem item;
    Node* next;
};

Node* head = NULL;

// 3. QUEUE for orders
struct OrderNode {
    int orderId;
    OrderNode* next;
};

OrderNode* front = NULL;
OrderNode* rear = NULL;

// ==================== ALGORITHMS ====================

// 4. BUBBLE SORT - Sort by price
void bubbleSort() {
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9 - i; j++) {
            if (menu[j].price > menu[j + 1].price) {
                // Swap
                MenuItem temp = menu[j];
                menu[j] = menu[j + 1];
                menu[j + 1] = temp;
            }
        }
    }
    cout << "\n Menu sorted by price (low to high)!\n";
}

// 5. LINEAR SEARCH - Find item by ID
void searchItem(int id) {
    for (int i = 0; i < 10; i++) {
        if (menu[i].id == id) {
            cout << "\n? Item Found!\n";
            cout << "ID: " << menu[i].id << endl;
            cout << "Name: " << menu[i].name << endl;
            cout << "Price: Rs." << menu[i].price << endl;
            cout << "Stock: " << menu[i].stock << endl;
            return;
        }
    }
    cout << "\n Item not found!\n";
}

// ==================== FILE HANDLING ====================

void saveToCSV() {
    // Save menu to CSV for Java GUI
    ofstream file("menu.csv");
    file << "ID,Name,Price,Stock\n";
    
    for (int i = 0; i < 10; i++) {
        file << menu[i].id << ","
             << menu[i].name << ","
             << menu[i].price << ","
             << menu[i].stock << "\n";
    }
    file.close();
    
    cout << "\n Menu saved \n";

}

void loadFromCSV() {
    ifstream file("menu.csv");
    if (!file) {
     cout << "Creating default menu...\n";
        return;
    }
    
    string line;
    getline(file, line); // Skip header
    
    int i = 0;
    while (getline(file, line) && i < 10) {
        // Simple CSV parsing
        int comma1 = line.find(',');
        int comma2 = line.find(',', comma1 + 1);
        int comma3 = line.find(',', comma2 + 1);
        
        if (comma1 != -1) {
            // Convert strings manually
            string idStr = line.substr(0, comma1);
            string nameStr = line.substr(comma1 + 1, comma2 - comma1 - 1);
            string priceStr = line.substr(comma2 + 1, comma3 - comma2 - 1);
            string stockStr = line.substr(comma3 + 1);
            
            // Convert to numbers - FIXED: Changed range-based for loops to traditional loops
            menu[i].id = 0;
            for (size_t j = 0; j < idStr.size(); ++j) {
                char c = idStr[j];
                if (c >= '0' && c <= '9') {
                    menu[i].id = menu[i].id * 10 + (c - '0');
                }
            }
            
            menu[i].name = nameStr;
            
            menu[i].price = 0;
            for (size_t j = 0; j < priceStr.size(); ++j) {
                char c = priceStr[j];
                if (c >= '0' && c <= '9') {
                    menu[i].price = menu[i].price * 10 + (c - '0');
                }
            }
            
            menu[i].stock = 0;
            for (size_t j = 0; j < stockStr.size(); ++j) {
                char c = stockStr[j];
                if (c >= '0' && c <= '9') {
                    menu[i].stock = menu[i].stock * 10 + (c - '0');
                }
            }
            
            i++;
        }
    }
    file.close();
    cout << " Menu loaded from CSV!\n";
}

// ==================== LINKED LIST OPERATIONS ====================

void createLinkedList() {
    // Clear existing list
    while (head != NULL) {
        Node* temp = head;
        head = head->next;
        delete temp;
    }
    
    head = NULL;
    Node* tail = NULL;  // ? ADD THIS
    
    // Create new linked list from array
    for (int i = 0; i < 10; i++) {
        Node* newNode = new Node();
        newNode->item = menu[i];
        newNode->next = NULL;    // ? CHANGED
        
        if (head == NULL) {
            head = tail = newNode;
        } else {
            tail->next = newNode;
            tail = newNode;
        }
    }
}
void displayLinkedList() {
    if (head == NULL) {
        cout << "Menu is empty!\n";
        return;
    }
    
    cout << "\n========== MENU =============\n";
    cout << "ID\tName\t\tPrice\tStock\n";
    cout << "------------------------------------\n";
    
    Node* temp = head;
    while (temp != NULL) {
        cout << temp->item.id << "\t";
        cout << temp->item.name;
        
        // Formatting
        if (temp->item.name.length() < 8) {
            cout << "\t\t";
        } else {
            cout << "\t";
        }
        
        cout << "Rs." << temp->item.price << "\t";
        cout << temp->item.stock << endl;
        
        temp = temp->next;
    }
}

// ==================== QUEUE OPERATIONS ====================

void placeOrder(int itemId, int quantity) {
    // Check stock
    for (int i = 0; i < 10; i++) {
        if (menu[i].id == itemId) {
            if (menu[i].stock >= quantity) {
                menu[i].stock -= quantity;
                
                // Generate order ID (1000-9999)
                int orderId = 1000 + (rand() % 9000);
                
                // Add to order queue
                OrderNode* newNode = new OrderNode();
                newNode->orderId = orderId;
                newNode->next = NULL;
                
                if (rear == NULL) {
                    front = rear = newNode;
                } else {
                    rear->next = newNode;
                    rear = newNode;
                }
                
                cout << "\n Order placed!\n";
                cout << "Order ID: " << orderId << endl;
                cout << "Item: " << menu[i].name << endl;
                cout << "Quantity: " << quantity << endl;
                cout << "Total: Rs." << (menu[i].price * quantity) << endl;
                
                // Save order to CSV
                ofstream orderFile("orders.csv", ios::app);
                orderFile << orderId << "," 
                         << menu[i].name << "," 
                         << quantity << ","
                         << (menu[i].price * quantity) << "\n";
                orderFile.close();
                
                return;
            } else {
                cout << "\n Not enough stock! Available: " << menu[i].stock << endl;
                return;
            }
        }
    }
    cout << "\n Item not found!\n";
}

void processOrder() {
    if (front == NULL) {
        cout << "\nNo orders to process!\n";
        return;
    }
    
    cout << "\n=== Processing Order ===\n";
    cout << "Order ID: " << front->orderId << endl;
    cout << "Status: COMPLETED\n";
    
    // Save to sales CSV
    ofstream salesFile("sales.csv", ios::app);
    salesFile << front->orderId << ",PROCESSED\n";
    salesFile.close();
    
    // Remove from queue
    OrderNode* temp = front;
    front = front->next;
    if (front == NULL) {
        rear = NULL;
    }
    delete temp;
}

void showQueue() {
    if (front == NULL) {
        cout << "\nNo orders in queue.\n";
        return;
    }
    
    cout << "\n=== PENDING ORDERS ===\n";
    OrderNode* temp = front;
    while (temp != NULL) {
        cout << "Order #" << temp->orderId << endl;
        temp = temp->next;
    }
}

// ==================== MAIN FUNCTION ====================

int main() {
    // Initialize random seed
    srand(time(0));
    cout << "========================================\n";
    cout << "   CAFETERIA MANAGEMENT SYSTEM (C++)\n";
    cout << "   Data Structures Project - Backend\n";
    cout << "========================================\n\n";

    
    // Initialize
    loadFromCSV();
    createLinkedList();
    
    int choice;
    do {
        cout << "\n========== MAIN MENU ==========\n";
        cout << "1. View Menu\n";
        cout << "2. Add Menu Item\n";
        cout << "3. Search Item\n";
        cout << "4. Place Order\n";
        cout << "5. Process Order\n";
        cout << "6. Show Order Queue\n";
        cout << "7. Sort Menu \n";
        cout << "8. Save\n";
        cout << "9. Exit\n";
        cout << "Enter Your Choice: ";
        
        cin >> choice;
        
        switch(choice) {
            case 1:
                displayLinkedList();
                break;
                
            case 2: {
                cout << "\nEnter ID (11-20): ";
                int id;
                cin >> id;
                cin.ignore();
                
                // Simple addition - for demonstration
                if (id >= 11 && id <= 20) {
                    cout << "Item added! (Demo only)\n";
                } else {
                    cout << "ID must be 11-20\n";
                }
                break;
            }
                
            case 3: {
                int searchId;
                cout << "\nEnter ID to search (1-10): ";
                cin >> searchId;
                searchItem(searchId);
                break;
            }
                
            case 4: {
                int itemId, quantity;
                cout << "\nEnter Item ID (1-10): ";
                cin >> itemId;
                cout << "Enter Quantity: ";
                cin >> quantity;
                placeOrder(itemId, quantity);
                break;
            }
                
            case 5:
                processOrder();
                break;
                
            case 6:
                showQueue();
                break;
                
            case 7:
                bubbleSort();
                createLinkedList(); // Update linked list after sorting
                break;
                
            case 8:
                saveToCSV();
                break;
                
            case 9:
                cout << "\nThank you! Data saved.\n";
                // Save final state
                saveToCSV();
                break;
                
            default:
                cout << "Invalid choice!\n";
        }
        
        // Pause
        if (choice != 9) {
            cout << "\nPress Enter to continue...";
            cin.ignore();
            cin.get();
        }
        
    } while (choice != 9);
    
    return 0;
}
