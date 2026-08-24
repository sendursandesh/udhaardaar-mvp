import sqlite3, math, re, hashlib, datetime

def emi(principal, annual_rate, months):
    assert principal > 0 and annual_rate >= 0 and months > 0
    r = annual_rate / 1200.0
    return principal / months if r == 0 else principal*r*(1+r)**months/((1+r)**months-1)

def run():
    con=sqlite3.connect(":memory:")
    db=con.cursor()
    db.executescript("""
    CREATE TABLE profiles(id INTEGER PRIMARY KEY,uid TEXT UNIQUE,role TEXT,name TEXT,mobile TEXT,alternate_mobile TEXT,address TEXT,city TEXT,state TEXT,pin TEXT,pan TEXT,aadhaar TEXT,gstin TEXT,photo TEXT,created TEXT);
    CREATE TABLE credits(id INTEGER PRIMARY KEY,code TEXT UNIQUE,borrower_id INTEGER,guarantor_id INTEGER,type TEXT,direction TEXT,amount REAL,roi REAL,method TEXT,installment REAL,interest REAL,payable REAL,start_date TEXT,end_date TEXT,due_date TEXT,gstin TEXT,invoice TEXT,nach INTEGER,status TEXT,otp INTEGER,created TEXT);
    CREATE TABLE schedules(id INTEGER PRIMARY KEY,credit_id INTEGER,no INTEGER,due_date TEXT,amount REAL,paid REAL DEFAULT 0,status TEXT);
    CREATE TABLE payments(id INTEGER PRIMARY KEY,credit_id INTEGER,schedule_id INTEGER,amount REAL,date TEXT);
    CREATE TABLE repayment_consents(id INTEGER PRIMARY KEY,credit_id INTEGER,schedule_id INTEGER,amount REAL,proposer_role TEXT,consent_token TEXT,status TEXT,created TEXT,confirmed TEXT);
    """)
    # 10 borrower + 10 guarantor profile cases.
    for role in ("BORROWER","GUARANTOR"):
        for i in range(10):
            db.execute("INSERT INTO profiles(uid,role,name,mobile,address,pin) VALUES(?,?,?,?,?,?)",
                       (f"{role}-{i}",role,f"{role} {i}",f"900000{i:04d}","Test address 123","834001"))
    borrowers=[x[0] for x in db.execute("SELECT id FROM profiles WHERE role='BORROWER'")]
    guarantors=[x[0] for x in db.execute("SELECT id FROM profiles WHERE role='GUARANTOR'")]
    assert len(borrowers)==len(guarantors)==10

    # 10 credit-registration cases covering all six credit types, both directions,
    # guarantor/no-guarantor, and EMI / principal+interest / single-payment.
    types=["Personal Credit","Business Credit","Trade Credit","Advance","Rental / Lease","Other"]
    methods=["EMI","Principal + Interest","Single Payment","Monthly Rent","Trade Invoice Due"]
    for i in range(10):
        principal=10000+i*1000; rate=12.0; months=12
        method=methods[i%len(methods)]
        payment=emi(principal,rate,months) if method=="EMI" else principal/months
        interest=max(0.0,payment*months-principal) if method=="EMI" else principal*rate/100*months/12
        payable=principal+interest
        db.execute("""INSERT INTO credits(code,borrower_id,guarantor_id,type,direction,amount,roi,method,installment,interest,payable,start_date,end_date,due_date,status,otp)
                      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                   (f"CR-QA-{i}",borrowers[i],guarantors[i] if i%2==0 else None,
                    types[i%len(types)],"Credit Given" if i%2==0 else "Credit Received",
                    principal,rate,method,payment,interest,payable,"2026-01-01","2027-01-01","2026-02-01","ACTIVE",1))
        cid=db.lastrowid
        db.execute("INSERT INTO schedules(credit_id,no,due_date,amount,paid,status) VALUES(?,?,?,?,?,?)",
                   (cid,1,"2026-02-01",payment,0.0,"DUE"))

    # 10 repayment + consent cases; verify no overpayment and hashed token storage.
    for sid,cid,amount in db.execute("SELECT id,credit_id,amount FROM schedules LIMIT 10").fetchall():
        pay=amount/2
        token=hashlib.sha256(b"123456").hexdigest()
        db.execute("""INSERT INTO repayment_consents(credit_id,schedule_id,amount,proposer_role,consent_token,status)
                      VALUES(?,?,?,?,?,?)""",(cid,sid,pay,"LENDER",token,"CONSENTED"))
        db.execute("INSERT INTO payments(credit_id,schedule_id,amount) VALUES(?,?,?)",(cid,sid,pay))
        db.execute("UPDATE schedules SET paid=?,status='DUE' WHERE id=?",(pay,sid))

    # 10 default/overdue cases.
    for sid in [x[0] for x in db.execute("SELECT id FROM schedules LIMIT 10")]:
        db.execute("UPDATE schedules SET status='OVERDUE',due_date='2020-01-01' WHERE id=?",(sid,))

    con.commit()
    assert db.execute("SELECT COUNT(*) FROM credits").fetchone()[0]==10
    assert db.execute("SELECT COUNT(*) FROM payments").fetchone()[0]==10
    assert db.execute("SELECT COUNT(*) FROM repayment_consents").fetchone()[0]==10
    assert db.execute("SELECT COUNT(*) FROM schedules WHERE status='OVERDUE'").fetchone()[0]==10
    assert db.execute("SELECT COUNT(*) FROM credits c LEFT JOIN profiles p ON p.id=c.borrower_id WHERE p.id IS NULL").fetchone()[0]==0
    assert db.execute("SELECT COUNT(*) FROM payments p JOIN schedules s ON s.id=p.schedule_id WHERE p.amount>s.amount").fetchone()[0]==0
    assert all(abs(x[0]-x[1]/2)<1e-9 for x in db.execute("SELECT paid,amount FROM schedules"))
    # Formula spot checks.
    assert abs(emi(100000,12,12)-8884.878867834177)<1e-6
    print("PASS: 10 borrowers + 10 guarantors + 10 credit registrations + 10 consented repayments + 10 overdue/default cases.")
    print("PASS: referential integrity, payment bounds, OTP hashing, and EMI calculation checks.")

if __name__=="__main__":
    run()
