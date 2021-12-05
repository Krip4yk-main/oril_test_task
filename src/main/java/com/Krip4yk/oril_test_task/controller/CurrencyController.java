package com.Krip4yk.oril_test_task.controller;

import com.Krip4yk.oril_test_task.JSONReader;
import com.Krip4yk.oril_test_task.exception.CurrencyNotFoundException;
import com.Krip4yk.oril_test_task.model.Currency;
import com.Krip4yk.oril_test_task.repository.CurrencyRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class CurrencyController {
    //Autowired
    final
    CurrencyRepository currencyRepository;

    public CurrencyController(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    //get all
    @GetMapping("/currency")
    public List getAllNotes() {
        return currencyRepository.findAll();
    }

    //create
    @PostMapping("/currency")
    public Currency createNote(@Valid @RequestBody Currency currency) {
        return (Currency) currencyRepository.save(currency);
    }

    //get by id
    @GetMapping("/currency/{id}")
    public Currency getNoteById(@PathVariable(value = "id") Integer currencyId) throws Throwable {
        return (Currency) currencyRepository.findById(currencyId)
                .orElseThrow(() -> new CurrencyNotFoundException(currencyId));
    }

    // update
    @PutMapping("/currency/{id}")
    public Currency updateNote(@PathVariable(value = "id") Integer currencyId,
                           @Valid @RequestBody Currency currencyDetails) throws Throwable {

        Currency currency = (Currency) currencyRepository.findById(currencyId)
                .orElseThrow(() -> new CurrencyNotFoundException(currencyId));

        currency.setName(currencyDetails.getName());
        currency.setValue(currencyDetails.getValue());

        Currency updatedCurrency = (Currency) currencyRepository.save(currency);
        return updatedCurrency;
    }

    // Delete
    @DeleteMapping("/currency/{id}")
    public ResponseEntity deleteCurrency(@PathVariable(value = "id") Integer currencyId) throws Throwable {
        Currency currency = (Currency) currencyRepository.findById(currencyId)
                .orElseThrow(() -> new CurrencyNotFoundException(currencyId));

        currencyRepository.delete(currency);
        return ResponseEntity.ok().build();
    }

    private final Logger log = LoggerFactory.getLogger(com.Krip4yk.oril_test_task.controller.CurrencyController.class);

    @Scheduled(cron = "* */10 * * * *")
    public void getCurrency(){
        String requestURL = "https://cex.io/api/last_price/";
        String btc="BTC/USD", eth="ETH/USD", xrp="XRP/USD";
        double BTC=0, ETH=0, XRP=0;
        try {
            BTC = Double.parseDouble(new JSONReader().readJsonFromUrl(requestURL+btc).get("lprice").toString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            ETH = Double.parseDouble(new JSONReader().readJsonFromUrl(requestURL+eth).get("lprice").toString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            XRP = Double.parseDouble(new JSONReader().readJsonFromUrl(requestURL+xrp).get("lprice").toString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        createNote(new Currency("BTC/USD", BTC));
        createNote(new Currency("ETH/USD", ETH));
        createNote(new Currency("XRP/USD", XRP));

        //log.info("Current currencies\nBTC/USD: "+BTC+"\nETH/USD: "+ETH+"\nXRP/USD:"+XRP);
    }

    @GetMapping("/cryptocurrencies/minprice")
    public Currency getMinPrice(@RequestParam("name") String name) {
        SortCurrencies sortCurrencies = new SortCurrencies(getAllNotes());
        switch (name) {
            case "BTC/USD":{
                return getMin(sortCurrencies.getBtc());
            }
            case "ETH/USD":{
                return getMin(sortCurrencies.getEth());
            }
            case "XRP/USD":{
                return getMin(sortCurrencies.getXrp());
            }
            default:
                throw new InputMismatchException();
        }
    }

    @GetMapping("/cryptocurrencies/maxprice")
    public Currency getMaxPrice(@RequestParam("name") String name) {
        SortCurrencies sortCurrencies = new SortCurrencies(getAllNotes());
        switch (name) {
            case "BTC/USD":{
                return getMax(sortCurrencies.getBtc());
            }
            case "ETH/USD":{
                return getMax(sortCurrencies.getEth());
            }
            case "XRP/USD":{
                return getMax(sortCurrencies.getXrp());
            }
            default:
                throw new InputMismatchException();
        }
    }

    @GetMapping("/cryptocurrencies")
    public List getPage(@RequestParam("name") String name, @RequestParam("page") Integer page, @RequestParam("size") Integer size) {
        SortCurrencies sortCurrencies = new SortCurrencies(getAllNotes());
        switch (name) {
            case "BTC/USD":{
                List<Currency> sorted_value = Sort(sortCurrencies.getBtc());
                int length = sorted_value.size();
                if (page*size > length) throw new InputMismatchException();
                return sorted_value.subList((page*size), ((page*size)+size > length) ? length-1 : (page*size)+size);
            }
            case "ETH/USD":{
                List<Currency> sorted_value = Sort(sortCurrencies.getEth());
                int length = sorted_value.size();
                if (page*size > length) throw new InputMismatchException();
                return sorted_value.subList((page*size), ((page*size)+size > length) ? length-1 : (page*size)+size);
            }
            case "XRP/USD":{
                List<Currency> sorted_value = Sort(sortCurrencies.getXrp());
                int length = sorted_value.size();
                if (page*size > length) throw new InputMismatchException();
                return sorted_value.subList((page*size), ((page*size)+size > length) ? length-1 : (page*size)+size);
            }
            default:
                throw new InputMismatchException();
        }
    }

    @GetMapping("/cryptocurrencies/csv")
    public void CsvReport(HttpServletResponse servletResponse) throws IOException {
        servletResponse.setContentType("text/csv");
        servletResponse.addHeader("Content-Disposition","attachment; filename=" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv");
        writeCurrenciesToCsv(servletResponse.getWriter());
    }

    public void writeCurrenciesToCsv(Writer writer) {
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            csvPrinter.printRecord("SEP=,");
            csvPrinter.printRecord("Currency Name", "Min Price", "Max Price");
            csvPrinter.printRecord("BTC/USD", getMinPrice("BTC/USD").getValue(), getMaxPrice("BTC/USD").getValue());
            csvPrinter.printRecord("ETH/USD", getMinPrice("ETH/USD").getValue(), getMaxPrice("ETH/USD").getValue());
            csvPrinter.printRecord("XRP/USD", getMinPrice("XRP/USD").getValue(), getMaxPrice("XRP/USD").getValue());
        } catch (IOException e) {
            log.error("Error While writing CSV ", e);
        }
    }

    public Currency getMin(List<Currency> value) {
        Currency res = value.get(0);
        double min = res.getValue();
        for (Currency c : value) {
            if (c.getValue()<=min) {
                if (c.getValue()==min) {
                    if (c.getCreatedAt().isAfter(res.getCreatedAt())) {
                        res = c;
                    }
                } else {
                    min = c.getValue();
                    res = c;
                }
            }
        }
        return res;
    }
    public Currency getMax(List<Currency> value) {
        Currency res = value.get(0);
        double max = res.getValue();
        for (Currency c : value) {
            if (c.getValue()>=max) {
                if (c.getValue()==max) {
                    if (c.getCreatedAt().isAfter(res.getCreatedAt())) {
                        res = c;
                    }
                } else {
                    max = c.getValue();
                    res = c;
                }
            }
        }
        return res;
    }
    public class SortCurrencies {
        private List<Currency> value;
        private List<Currency> btc, eth, xrp;
        SortCurrencies(List<Currency> value) {
            this.value = value;
            btc = new ArrayList<>();
            eth = new ArrayList<>();
            xrp = new ArrayList<>();
            sort();
        }
        SortCurrencies(){
            value = new ArrayList<>();
            btc = new ArrayList<>();
            eth = new ArrayList<>();
            xrp = new ArrayList<>();
        }

        public void sort() {
            List<Currency> currencies = getAllNotes();
            for (Currency c : currencies)
                if (c.getName().equals("BTC/USD")) btc.add(c);
                else if (c.getName().equals("ETH/USD")) eth.add(c);
                else if (c.getName().equals("XRP/USD")) xrp.add(c);
        }

        public List<Currency> getValue() {
            return value;
        }

        public void setValue(List<Currency> value) {
            this.value = value;
        }

        public List<Currency> getBtc() {
            return btc;
        }

        public void setBtc(List<Currency> btc) {
            this.btc = btc;
        }

        public List<Currency> getEth() {
            return eth;
        }

        public void setEth(List<Currency> eth) {
            this.eth = eth;
        }

        public List<Currency> getXrp() {
            return xrp;
        }

        public void setXrp(List<Currency> xrp) {
            this.xrp = xrp;
        }
    }
    public class SortByValue implements Comparator<Currency> {
        public int compare(Currency a, Currency b) {
            return Double.compare(a.getValue(), b.getValue());
        }
    }
    public List<Currency> Sort(List<Currency> value) {
        Collections.sort(value, new SortByValue());
        return value;
    }
}
