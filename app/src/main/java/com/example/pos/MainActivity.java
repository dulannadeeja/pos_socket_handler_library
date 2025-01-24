package com.example.pos;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.core.network.NetworkServiceDiscoveryManagerImpl;
import com.example.customerdisplayhandler.model.ComboGroup;
import com.example.customerdisplayhandler.model.Customer;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.DisplayUpdates;
import com.example.customerdisplayhandler.model.Modifier;
import com.example.customerdisplayhandler.model.ReceiptItem;
import com.example.customerdisplayhandler.model.ReceiptPoint;
import com.example.customerdisplayhandler.model.Terminal;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.utils.JsonUtilImpl;
import com.example.pos.ui.AddCustomerDisplayFragment;
import com.example.pos.ui.CustomerDisplaySettingsDialogFragment;
import com.example.pos.ui.CustomerDisplayViewModel;
import com.example.pos.ui.FailedCustomerDisplaysFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private ICustomerDisplayManager customerDisplayManager;
    private IJsonUtil jsonUtil;
    private CustomerDisplayViewModel customerDisplayViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        App app = (App) getApplication();
        customerDisplayManager = app.getCustomerDisplayManager();
        customerDisplayManager.setTerminalID("TERMINAL01");

        customerDisplayViewModel = new ViewModelProvider(this).get(CustomerDisplayViewModel.class);
        customerDisplayViewModel.setCustomerDisplayManager(customerDisplayManager);

        customerDisplayViewModel.getToastMessage()
                .observe(this, this::showToast);

        jsonUtil = new JsonUtilImpl();

        Button customerDisplayButton = findViewById(R.id.go_to_customer_display_settings);
        customerDisplayButton.setOnClickListener(v -> {
            showCustomerDisplaySettingsFragment();
        });

        Button sendUpdatesButton = findViewById(R.id.send_to_customer_display);
        sendUpdatesButton.setOnClickListener(v -> {
            sendDisplayUpdates(createSampleDisplayUpdates1());
        });

        Button sendUpdatesButton2 = findViewById(R.id.send_to_customer_display_2);
        sendUpdatesButton2.setOnClickListener(v -> {
            sendDisplayUpdates(createSampleDisplayUpdates2());
        });

        Button sendUpdatesButton3 = findViewById(R.id.send_to_customer_display_3);
        sendUpdatesButton3.setOnClickListener(v -> {
            sendDisplayUpdates(createSampleDisplayUpdates3());
        });

    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showCustomerDisplaySettingsFragment() {
        CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = CustomerDisplaySettingsDialogFragment.newInstance();
        customerDisplaySettingsDialogFragment.show(getSupportFragmentManager(), CustomerDisplaySettingsDialogFragment.TAG);
    }

    private void showAddCustomerDisplayFragment() {
        AddCustomerDisplayFragment addCustomerDisplayFragment = AddCustomerDisplayFragment.newInstance();
        addCustomerDisplayFragment.show(getSupportFragmentManager(), "add_customer_display");
    }

    private void sendDisplayUpdates(DisplayUpdates displayUpdates){
        customerDisplayManager.sendUpdatesToCustomerDisplays(displayUpdates, new ICustomerDisplayManager.OnSendUpdatesListener() {
            @Override
            public void onAllUpdatesSentWithSuccess() {
                showToast("All updates sent successfully");
            }

            @Override
            public void onSomeUpdatesFailed(List<Pair<CustomerDisplay, Boolean>> failedCustomerDisplays) {
                onSendDisplayUpdatesFailed(failedCustomerDisplays);
                failedCustomerDisplays.forEach(
                        customerDisplayBooleanPair -> {
                            Log.e("MainActivity",customerDisplayBooleanPair.first.getCustomerDisplayID() + " ,status: " + customerDisplayBooleanPair.second);
                        }
                );
            }

            @Override
            public void onSystemError(String errorMessage) {
                showToast("System error: " + errorMessage);
            }
        });
    }

    private void onSendDisplayUpdatesFailed(List<Pair<CustomerDisplay, Boolean>> failedCustomerDisplays) {
        List<CustomerDisplay> listOfFailedCustomerDisplays = failedCustomerDisplays.stream()
                .filter(customerDisplayBooleanPair -> !customerDisplayBooleanPair.second)
                .map(customerDisplayBooleanPair -> customerDisplayBooleanPair.first)
                .collect(Collectors.toList());
        String json = jsonUtil.toJson(listOfFailedCustomerDisplays);
        FailedCustomerDisplaysFragment failedCustomerDisplaysFragment = FailedCustomerDisplaysFragment.newInstance(json);
        failedCustomerDisplaysFragment.show(getSupportFragmentManager(), FailedCustomerDisplaysFragment.TAG);
    }

    // TODO: remove this temporary function.
    private DisplayUpdates createSampleDisplayUpdates1() {

        ReceiptItem receiptItem1 = new ReceiptItem.Builder()
                .setItemName("Combo 3 with variants (f/a)")
                .setItemQty(1)
                .setItemPrice(200)
                .setDualItemPrice(200)
                .setCreditNote(1)
                .setItemOrder(0)
                .setCreditNoteQty(0)
                .setCreditNoteValue(0)
                .setCrnId("")
                .setOriginalLineNo("1-631")
                .setKotNote("")
                .setItemRemark("")
                .setUniqueId(0)
                .setModifierList(new ArrayList<>())
                .setItemTaxList(new ArrayList<>())
                .setComboList(new ArrayList<>())
                .build();

        List<ComboGroup> comboGroupList = new ArrayList<>();
        List<String> items = new ArrayList<>();
        items.add("combo 3 with variants (f/a) x 1");
        items.add("combo 3 with variants (f/c) x 2");
        items.add("combo 3 with variants (f/d) x 3");
        ComboGroup comboGroup = new ComboGroup("Combo set test", items);
        ComboGroup comboGroup1 = new ComboGroup("Combo set test 1", items);
        comboGroupList.add(comboGroup);
        comboGroupList.add(comboGroup1);

        Modifier modifier = new Modifier.Builder()
                .setName("name of the modifier is here")
                .setCode("here is modifier code")
                .setCost(100)
                .setIsBackup(0)
                .setId(1)
                .setIsSelect(1)
                .setPrice(100)
                .setQty(1)
                .setStatus(1)
                .build();

        Modifier modifier1 = new Modifier.Builder()
                .setName("name of the modifier is here")
                .setCode("here is modifier code 1")
                .setCost(100)
                .setIsBackup(0)
                .setId(1)
                .setIsSelect(1)
                .setPrice(100)
                .setQty(1)
                .setStatus(1)
                .build();

        Modifier modifier2 = new Modifier.Builder()
                .setName("name of the modifier is here")
                .setCode("here is modifier code 2")
                .setCost(100)
                .setIsBackup(0)
                .setId(1)
                .setIsSelect(1)
                .setPrice(100)
                .setQty(1)
                .setStatus(1)
                .build();

        List<Modifier> modifierList = new ArrayList<>();
        modifierList.add(modifier);
        modifierList.add(modifier1);
        modifierList.add(modifier2);


        ReceiptItem receiptItem2 = new ReceiptItem.Builder()
                .setItemName("Comboo product")
                .setItemQty(1)
                .setItemPrice(100)
                .setDualItemPrice(100)
                .setCreditNote(1)
                .setItemOrder(0)
                .setCreditNoteQty(0)
                .setCreditNoteValue(0)
                .setCrnId("")
                .setOriginalLineNo("1-630")
                .setKotNote("")
                .setItemRemark("")
                .setUniqueId(0)
                .setModifierList(modifierList)
                .setItemTaxList(new ArrayList<>())
                .setComboList(comboGroupList)
                .build();

        Customer customer = new Customer.Builder()
                .setCustomerId("COM1")
                .setCustomerFirstName("John")
                .setCustomerLastName("Doe")
                .setPhone("xxxx-xxxxxxx")
                .setEmail("customer@mail.com")
                .setLoyaltyPoint(0f)
                .setTotalLoyaltyPoint(0f)
                .setCustomerOutstanding(0f)
                .setLoyaltyStatus(0f)
                .setCreditLimit("")
                .setCustomerCode("")
                .build();

        Terminal terminal = new Terminal.Builder()
                .setTerminalId("")
                .setTerminalName("")
                .setDownloadUrl("")
                .setBackupTime("")
                .setFileType("")
                .setAppType("")
                .setCurrentAppVersion("")
                .build();

        ReceiptPoint receiptPoint = new ReceiptPoint.Builder()
                .setMainInvoiceNumber("")
                .setDate("")
                .setCustomerId("")
                .setReceiptAmount(0.0f)
                .setAddedPoint(0.0f)
                .setLoyaltyLevel("")
                .setEarnPoint(0.0f)
                .setRedeemPoint(0.0f)
                .build();


        DisplayUpdates displayUpdates = new DisplayUpdates.Builder()
                .setReceiptStatus(1)
                .setDualPricingStatus(1)
                .setCustomerDisplayHeader("Test Business Name")
                .setDefaultCartHeader("price name 1")
                .setDualPricingCartHeader("price name 2")
                .setDualPricingCardEnable(true)
                .setDualPricingCashEnable(true)
                .setCustomerDisplayBg("https://png.pngtree.com/thumb_back/fh260/background/20240327/pngtree-supermarket-aisle-with-empty-shopping-cart-at-grocery-store-retail-business-image_15646095.jpg")
                .setCustomerDisplayLogo("https://png.pngtree.com/png-vector/20220930/ourmid/pngtree-shopping-logo-design-for-online-store-website-png-image_6239056.png")
                .setDecimalSeparator('.')
                .setThousandSeparator(',')
                .setDecimalPlaces(2)
                .setLanguage("English")
                .setReceiptName("Test Receipt")
                .setCreatedBY("Test User")
                .setMainInvoiceNumber("123456")
                .setDateTime("2021-10-10 10:10:10")
                .setBillNote("Test Bill Note")
                .setCashier("Test Cashier")
                .setOrderType("Delivery")
                .setSubTotal(2800)
                .setDualPricingSubTotal(2800)
                .setDiscount(200)
                .setDualPricingDiscount(200)
                .setTax(200)
                .setDualPricingTax(200)
                .setReceiptTotal(3000)
                .setDualPricingReceiptTotal(3000)
                .setNumberOfItem(6)
                .setNumberOfQty(6)
                .setReceiptPaidValue(0)
                .setBalance(0)
                .setCustomer(customer)
                .setTerminal(terminal)
                .setReceiptPoint(receiptPoint)
                .setReceiptItemList(new ArrayList<ReceiptItem>() {{
                    add(receiptItem1);
                    add(receiptItem2);
                }})
                .build();
        return displayUpdates;
    }

    private DisplayUpdates createSampleDisplayUpdates2() {

        ReceiptItem receiptItem1 = new ReceiptItem.Builder()
                .setItemName("Deluxe Burger Combo")
                .setItemQty(2)
                .setItemPrice(300)
                .setDualItemPrice(320)
                .setCreditNote(0)
                .setItemOrder(1)
                .setCreditNoteQty(0)
                .setCreditNoteValue(0)
                .setCrnId("")
                .setOriginalLineNo("2-221")
                .setKotNote("No onions")
                .setItemRemark("Extra cheese added")
                .setUniqueId(101)
                .setModifierList(new ArrayList<>())
                .setItemTaxList(new ArrayList<>())
                .setComboList(new ArrayList<>())
                .build();

        List<ComboGroup> comboGroups = new ArrayList<>();
        List<String> comboItems = new ArrayList<>();
        comboItems.add("Deluxe Burger x 1");
        comboItems.add("Fries x 1");
        comboItems.add("Soda x 1");
        comboGroups.add(new ComboGroup("Meal Set", comboItems));

        Modifier modifier = new Modifier.Builder()
                .setName("Extra Bacon")
                .setCode("BACON01")
                .setCost(50)
                .setIsBackup(0)
                .setId(202)
                .setIsSelect(1)
                .setPrice(50)
                .setQty(1)
                .setStatus(1)
                .build();

        List<Modifier> modifiers = new ArrayList<>();
        modifiers.add(modifier);

        ReceiptItem receiptItem2 = new ReceiptItem.Builder()
                .setItemName("Veggie Salad")
                .setItemQty(1)
                .setItemPrice(150)
                .setDualItemPrice(170)
                .setCreditNote(0)
                .setItemOrder(2)
                .setCreditNoteQty(0)
                .setCreditNoteValue(0)
                .setCrnId("")
                .setOriginalLineNo("3-150")
                .setKotNote("Add dressing on the side")
                .setItemRemark("")
                .setUniqueId(102)
                .setModifierList(modifiers)
                .setItemTaxList(new ArrayList<>())
                .setComboList(comboGroups)
                .build();

        Customer customer = new Customer.Builder()
                .setCustomerId("CUST123")
                .setCustomerFirstName("Alice")
                .setCustomerLastName("Smith")
                .setPhone("123-456-7890")
                .setEmail("alice.smith@example.com")
                .setLoyaltyPoint(50.5f)
                .setTotalLoyaltyPoint(120.5f)
                .setCustomerOutstanding(0f)
                .setLoyaltyStatus(1f)
                .setCreditLimit("5000")
                .setCustomerCode("VIP001")
                .build();

        Terminal terminal = new Terminal.Builder()
                .setTerminalId("TERMINAL01")
                .setTerminalName("POS 1")
                .setDownloadUrl("")
                .setBackupTime("")
                .setFileType("")
                .setAppType("")
                .setCurrentAppVersion("v1.5.0")
                .build();

        ReceiptPoint receiptPoint = new ReceiptPoint.Builder()
                .setMainInvoiceNumber("INV20230101")
                .setDate("2023-01-01")
                .setCustomerId("CUST123")
                .setReceiptAmount(470.0f)
                .setAddedPoint(20.0f)
                .setLoyaltyLevel("Gold")
                .setEarnPoint(25.0f)
                .setRedeemPoint(5.0f)
                .build();

        DisplayUpdates displayUpdates = new DisplayUpdates.Builder()
                .setReceiptStatus(0)
                .setDualPricingStatus(1)
                .setCustomerDisplayHeader("Gourmet Restaurant")
                .setDefaultCartHeader("Standard Price")
                .setDualPricingCartHeader("Member Price")
                .setDualPricingCardEnable(false)
                .setDualPricingCashEnable(true)
                .setCustomerDisplayBg("https://example.com/background.jpg")
                .setCustomerDisplayLogo("https://example.com/logo.png")
                .setDecimalSeparator('.')
                .setThousandSeparator(',')
                .setDecimalPlaces(2)
                .setLanguage("English")
                .setReceiptName("Sample Receipt")
                .setCreatedBY("System Admin")
                .setMainInvoiceNumber("INV20230101")
                .setDateTime("2023-01-01 12:30:00")
                .setBillNote("Thank you for dining with us!")
                .setCashier("Jane Doe")
                .setOrderType("Dine-In")
                .setSubTotal(500)
                .setDualPricingSubTotal(540)
                .setDiscount(30)
                .setDualPricingDiscount(20)
                .setTax(50)
                .setDualPricingTax(60)
                .setReceiptTotal(470)
                .setDualPricingReceiptTotal(500)
                .setNumberOfItem(2)
                .setNumberOfQty(3)
                .setReceiptPaidValue(470)
                .setBalance(0)
                .setCustomer(customer)
                .setTerminal(terminal)
                .setReceiptPoint(receiptPoint)
                .setReceiptItemList(new ArrayList<ReceiptItem>() {{
                    add(receiptItem1);
                    add(receiptItem2);
                }})
                .build();

        return displayUpdates;
    }

    private DisplayUpdates createSampleDisplayUpdates3() {

        List<ComboGroup> comboGroups1 = new ArrayList<>();
        List<String> comboItems1 = new ArrayList<>();
        comboItems1.add("Cheeseburger x 1");
        comboItems1.add("Fries x 1");
        comboItems1.add("Soft Drink x 1");
        comboGroups1.add(new ComboGroup("Combo Meal A", comboItems1));

        List<ComboGroup> comboGroups2 = new ArrayList<>();
        List<String> comboItems2 = new ArrayList<>();
        comboItems2.add("Grilled Chicken x 1");
        comboItems2.add("Salad x 1");
        comboItems2.add("Iced Tea x 1");
        comboGroups2.add(new ComboGroup("Healthy Combo B", comboItems2));

        List<ComboGroup> comboGroups3 = new ArrayList<>();
        List<String> comboItems3 = new ArrayList<>();
        comboItems3.add("Veggie Pizza Slice x 1");
        comboItems3.add("Garlic Bread x 1");
        comboItems3.add("Lemonade x 1");
        comboGroups3.add(new ComboGroup("Vegetarian Delight C", comboItems3));

        List<Modifier> modifiers = new ArrayList<>();
        modifiers.add(new Modifier.Builder()
                .setName("Extra Cheese")
                .setCode("CHEESE01")
                .setCost(30)
                .setQty(1)
                .setIsSelect(1)
                .setPrice(30)
                .setId(301)
                .setStatus(1)
                .build());
        modifiers.add(new Modifier.Builder()
                .setName("Bacon Strips")
                .setCode("BACON02")
                .setCost(50)
                .setQty(2)
                .setIsSelect(1)
                .setPrice(100)
                .setId(302)
                .setStatus(1)
                .build());

        List<ReceiptItem> receiptItems = new ArrayList<>();
        receiptItems.add(new ReceiptItem.Builder()
                .setItemName("Combo Meal A")
                .setItemQty(1)
                .setItemPrice(450)
                .setDualItemPrice(470)
                .setItemOrder(1)
                .setKotNote("No ketchup on fries")
                .setComboList(comboGroups1)
                .build());
        receiptItems.add(new ReceiptItem.Builder()
                .setItemName("Healthy Combo B")
                .setItemQty(2)
                .setItemPrice(800)
                .setDualItemPrice(850)
                .setItemOrder(2)
                .setKotNote("Extra dressing on salad")
                .setComboList(comboGroups2)
                .setModifierList(modifiers)
                .build());
        receiptItems.add(new ReceiptItem.Builder()
                .setItemName("Vegetarian Delight C")
                .setItemQty(1)
                .setItemPrice(550)
                .setDualItemPrice(600)
                .setItemOrder(3)
                .setKotNote("Serve pizza slice warm")
                .setComboList(comboGroups3)
                .build());

        Customer customer = new Customer.Builder()
                .setCustomerId("CUST456")
                .setCustomerFirstName("John")
                .setCustomerLastName("Doe")
                .setPhone("987-654-3210")
                .setEmail("john.doe@example.com")
                .setLoyaltyPoint(100.0f)
                .setTotalLoyaltyPoint(250.0f)
                .setCustomerOutstanding(0.0f)
                .setLoyaltyStatus(1.0f)
                .setCreditLimit("3000")
                .setCustomerCode("REGULAR123")
                .build();

        Terminal terminal = new Terminal.Builder()
                .setTerminalId("TERMINAL02")
                .setTerminalName("POS 2")
                .setCurrentAppVersion("v2.0.0")
                .build();

        ReceiptPoint receiptPoint = new ReceiptPoint.Builder()
                .setMainInvoiceNumber("INV20240115")
                .setDate("2024-01-15")
                .setCustomerId("CUST456")
                .setReceiptAmount(1800.0f)
                .setAddedPoint(50.0f)
                .setLoyaltyLevel("Platinum")
                .setEarnPoint(60.0f)
                .setRedeemPoint(10.0f)
                .build();

        return new DisplayUpdates.Builder()
                .setReceiptStatus(1)
                .setDualPricingStatus(1)
                .setCustomerDisplayHeader("Family Restaurant")
                .setDefaultCartHeader("Regular Price")
                .setDualPricingCartHeader("Member Price")
                .setDualPricingCardEnable(true)
                .setDualPricingCashEnable(true)
                .setCustomerDisplayBg("https://example.com/background2.jpg")
                .setCustomerDisplayLogo("https://example.com/logo2.png")
                .setDecimalSeparator('.')
                .setThousandSeparator(',')
                .setDecimalPlaces(2)
                .setLanguage("English")
                .setReceiptName("Detailed Receipt")
                .setCreatedBY("Admin")
                .setMainInvoiceNumber("INV20240115")
                .setDateTime("2024-01-15 13:45:00")
                .setBillNote("Visit again for great meals!")
                .setCashier("Emily Johnson")
                .setOrderType("Takeaway")
                .setSubTotal(1800)
                .setDualPricingSubTotal(1920)
                .setDiscount(100)
                .setDualPricingDiscount(80)
                .setTax(120)
                .setDualPricingTax(140)
                .setReceiptTotal(1800)
                .setDualPricingReceiptTotal(1920)
                .setNumberOfItem(3)
                .setNumberOfQty(4)
                .setReceiptPaidValue(1800)
                .setBalance(0)
                .setCustomer(customer)
                .setTerminal(terminal)
                .setReceiptPoint(receiptPoint)
                .setReceiptItemList(receiptItems)
                .build();
    }



}