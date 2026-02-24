package com.trade_risk_system.config;

import com.trade_risk_system.model.Instrument;
import com.trade_risk_system.model.User;
import com.trade_risk_system.model.enums.Role;
import com.trade_risk_system.repository.InstrumentRepository;
import com.trade_risk_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;

    public DataInitializer(UserRepository userRepository,
            InstrumentRepository instrumentRepository) {
        this.userRepository = userRepository;
        this.instrumentRepository = instrumentRepository;
    }

    @Override
    public void run(String... args) {

        if (userRepository.count() == 0) {

            // ADMIN USER
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@traderisk.com"); // ✅ FIXED
            admin.setPassword("admin123");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            // TRADER USER
            User trader = new User();
            trader.setUsername("trader");
            trader.setEmail("trader@traderisk.com"); // ✅ FIXED
            trader.setPassword("trader123");
            trader.setRole(Role.TRADER);
            userRepository.save(trader);

            // SAMPLE INSTRUMENT
            Instrument inst = new Instrument();
            inst.setSymbol("AAPL");
            inst.setName("Apple Inc.");
            instrumentRepository.save(inst);

            System.out.println("Seed data inserted successfully.");
        }
    }
}
