package com.example.boost_product_data.service;

import com.example.boost_product_data.common.CommonEntities;
import com.github.javafaker.Color;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class FakeService {

    private final CommonEntities commonEntities;
    private final Faker koraeanFaker = new Faker(Locale.KOREA);
    private final Faker englishFaker = new Faker(Locale.ENGLISH);
    private final Random random = new Random();


    private  final String[] PATTERNS = {
            // 기본
            "솔리드", "무지", "Solid", "Plain",
            "스트라이프", "단가라", "보더", "Stripe", "Border", // (예: 아디다스)
            "체크", "Check", "Checkered", // (예: 반스)
            "도트", "땡땡이", "Polka Dot", "Dot",

            // 체크 세분화
            "깅엄", "타탄", "글렌", "하운드투스",
            "Gingham", "Tartan", "Plaid", "Glen Check", "Houndstooth",

            // 프린트
            "플로럴", "꽃무늬", "Floral",
            "페이즐리", "Paisley",
            "아가일", "Argyle",
            "카모플라쥬", "밀리터리", "Camo", "Camouflage", "Military",
            "애니멀 프린트", "레오파드", "호피", "지브라", "스네이크", "Animal Print", "Leopard", "Zebra", "Snake",
            "로고플레이", "그래픽", "레터링", "Logo", "Graphic", "Lettering",
            "타이다이", "Tie-dye",

            // 직조/질감/가공 (신발 특화)
            "헤링본", "케이블", "트위드", "자카드", "퀼팅", "자수", "엠보싱", "시어링",
            "Herringbone", "Cable", "Tweed", "Jacquard", "Quilting", "Embroidery", "Embossed", "Shearling",
            "컬러블록", "Color Block", "펀칭", "Perforated", "우븐", "Woven", "크로셰", "Crochet"
    };




    private  final String[] SEASONS_AND_USES = {
            // 시즌 (Season)
            "SS시즌", "FW시즌", "S/S", "F/W", "봄", "여름", "가을", "겨울",
            "Spring", "Summer", "Autumn", "Fall", "Winter",
            "사계절용", "올시즌", "All-Season", "간절기용", "환절기",

            // 용도 (TPO - 신발 특화)
            "데일리", "Daily", "일상용", "OOTD",
            "출근룩", "오피스룩", "Office Look", "Business Look",
            "하객룩", "데이트룩", "Wedding Guest", "Date Look",
            "등교룩", "캠퍼스룩", "School Look", "Campus Look",
            "원마일웨어", "홈웨어", "라운지웨어", "One-Mile Wear", "Homewear", "Loungewear",
            "바캉스룩", "휴가룩", "여행용", "Vacation", "Travel", "비치웨어", "Beachwear",
            "실내용", "Indoor", "실외용", "Outdoor",

            // 기능성 용도 (Functional Use)
            "운동용", "스포츠", "Sports", "Workout", "Gymwear",
            "러닝용", "Running", "워킹용", "Walking", "등산용", "Hiking",
            "방수", "Waterproof", "레인부츠", "Rainy Day",

            // 마케팅/판매 (Marketing)
            "시즌오프", "Season Off", "베스트셀러", "Best Seller", "스테디셀러", "Steady Seller",
            "시그니처", "Signature", "신상", "New Arrival",
            "한정판", "리미티드 에디션", "Limited Edition", "단독", "Exclusive",
            "콜라보", "Collaboration", "기획특가", "Special Price",
            "입문용", "선물용", "For Beginners", "For Gift"
    };




    private  final String[] MATERIALS = {
            // 갑피 (Upper) - 가죽류
            "천연가죽", "Genuine Leather", "소가죽", "Cowhide", "양가죽", "Lambskin",
            "스웨이드", "Suede", "누벅", "Nubuck", "에나멜", "페이턴트", "Patent Leather",
            "합성가죽", "인조가죽", "페이크 레더", "비건 레더",
            "Synthetic Leather", "Faux Leather", "Vegan Leather",

            // 갑피 (Upper) - 섬유류
            "면", "Cotton",
            "메시", "Mesh", "니트", "Knit", "플라이니트", "Flyknit",
            "나일론", "Nylon", "폴리에스터", "Polyester", "데님", "Denim",
            "고어텍스", "Gore-Tex", "네오프렌", "Neoprene",
            "코듀로이", "Corduroy", "벨벳", "Velvet", "플리스", "Fleece", "시어링", "Shearling",

            // 중창 (Midsole) - 쿠셔닝
            "EVA", "파일론", "Phylon", "폴리우레탄", "PU",
            "부스트폼", "Boost Foam", "리액트폼", "React Foam", "에어", "Air", "젤", "Gel",

            // 밑창 (Outsole) - 접지력
            "고무", "Rubber", "검솔", "Gum Sole", "카본 러버", "Carbon Rubber",
            "비브람", "Vibram", "클리어솔", "Clear Sole", "샤크솔", "Shark Sole",

            // 기타
            "코르크", "Cork", "우드", "Wood", "TPU"
    };

    private final String[] ADJECTIVES = {
            // 형태/핏 (Shape & Fit)
            "하이탑", "High-top", "미드탑", "Mid-top", "로우탑", "Low-top",
            "슬립온", "Slip-on", "레이스업", "Lace-up",
            "와이드핏", "발볼 넓은", "Wide Fit", "내로우핏", "발볼 좁은", "Narrow Fit",
            "플랫폼", "Platform", "청키한", "Chunky", "어글리", "Ugly",
            "슬림핏", "Slim Fit", "레귤러핏", "Regular Fit", "오버사이즈", "Oversized",
            "크롭", "Cropped", "롱", "Long", "스퀘어 토", "Square Toe", "포인티드 토", "Pointed Toe", "라운드 토", "Round Toe",

            // 착화감/기능성 (Comfort & Function)
            "편안한", "Comfortable", "푹신한", "쿠션감 좋은", "Cushioned",
            "가벼운", "Lightweight", "초경량", "Ultra-light",
            "지지력 좋은", "Supportive", "안정적인", "Stable",
            "통기성 좋은", "Breathable", "방수", "Waterproof", "발수", "Water-repellent",
            "미끄럼 방지", "Non-slip",
            "튼튼한", "Durable", "견고한", "Heavy Duty",
            "따뜻한", "Warm", "시원한", "Cool", "쿨링", "Quick-dry",

            // 일반 수식어 (General)
            "프리미엄", "Premium", "럭셔리", "Luxury", "익스클루시브", "Exclusive",
            "스타일리시", "Stylish", "모던", "Modern", "클래식", "Classic", "트렌디", "Trendy",
            "세련된", "고급스러운", "유니크한", "Unique", "힙한", "Hip", "아이코닉", "Iconic",
            "심플한", "Simple", "베이직", "Basic", "에센셜", "Essential",
            "감성적인", "Aesthetic", "귀여운", "Cute", "사랑스러운", "Lovely",
            "우아한", "Elegant", "깔끔한", "스포티", "Sporty", "애슬레틱", "Athletic",

            // 영어 전용
            "Versatile", "Timeless", "Ultimate"
    };


    // --- 👟 실제 신발 모델명 데이터셋 (약 80개로 확장) ---
    private  final String[] SHOE_MODELS = {
            "Air Force 1 '07 LV8", "Air Max Plus TN", "Air Max 270", "Air Max Excee",
            "VaporMax Flyknit 3", "Invincible 3", "Metcon 9", "Tempo Next% FlyEase",
            "Zoom X Streakfly", "V2K Run", "Air Humara", "Gamma Force", "Air Huarache",
            "Structure 25", "Renew Run 4", "Zoom Fly 5", "Pegasus 40", "Vomero 5",
            "React Infinity Run 4", "Air Max 97", "Dunk Low Retro SE", "Jordan 1 Mid SE Craft",
            "에어파스 1.32", "에오포스 1.123", "에이픽스", "제이팍스", "조이픽스", "마이포스",
            "맥스 스콜피온", "마운틴 플라이", "테일윈드 79",

            "Samba OG", "Gazelle Bold", "Campus 80s", "Retropy E5", "Response CL", "Rivalry Low",
            "Astir", "Runfalcon", "Terrex Swift R3", "Forum Bold", "Court Vision 2.0",
            "Continental 80", "Pureboost 22", "Cloudfoam Pure", "Fluidflow 2.0", "Niteball",
            "Ozweego Pure", "ZX 500", "Supercourt", "OZMILLEN", "SL 72", "ADIMATIC",
            "Racer TR21", "Duramo SL",

            "990v6", "998", "1500", "550", "408", "860v2", "X-Racer",
            "Fresh Foam X 1080v13", "FuelCell Rebel v4", "920", "M1300",
            "57/40", "327 V2", "RC-Sandal", "CT302", "574 Legacy", "More Trail v3",

            "Gel-1090V2", "Gel-Lyte III OG", "Gel-Venture 6", "Japan S", "Ex89",
            "Gel-Quantum 360 VII", "Tartheredge", "Gel-Nimbus 26", "Gel-Kayano Legacy",
            "Gel-Pulse 14", "Gel-Noosa Tri 15", "Metaspeed Sky+", "Gel-NYC RE", "GEL-FujiTrabuco 8",
            "젝 카야녹 31", "젤 카야녹 401", "직 카야녹 3111", "젤라튄 288", "졸리 보스 327",
            "NO.7 에디션", "조그 100 2", "재팬 S 23",

            "Odyssey Advanced", "RX Moc 3.0", "S/Lab Phantasm", "Speedcross Vario",
            "Sense Ride 5", "Outpulse GTX", "Index.01", "XT-QUEST 2", "XT-Slate Advanced",

            "Speedgoat 5", "Rocket X 2", "Tecton X 2", "Kawana", "Gaviota 4",
            "Clifton L Suede", "Tor Ultra Hi", "Anacapa Low GTX", "Challenger ATR 6",
            "마파테 스피트 2", "토르 하이",

            "Suede Classic XXI", "Cali Dream", "RS-X", "Future Rider",
            "Velophas", "Slipstream Lo", "Mayze Wedge", "Mirage Sport",
            "퓨마 스웨이드 50", "몬테카를로", "드리프트 캣", "트라이엄프 21",

            "Club C 85 Vintage", "Instapump Fury 95", "Classic Leather Legacy",
            "Zig Kinetica 2.5", "Nano X3", "Aztrek 96",
            "펌프 퓨리", "클럽 C 더블", "클래식 레트로", "고스트 15",

            "Old Skool", "Sk8-Low", "Slip-On Checkerboard", "Mid Skool 37 DX", "Bold Ni",
            "EVDNT Ultimatewaffle", "Knu Skool", "Style 36",
            "Chuck 70 Plus", "Run Star Hike High", "Pro Leather", "Weapon CX",
            "영 스쿨", "미들 스쿨", "스타필드", "척 테일러",

            "Disruptor II Premium", "Ray Tracer", "Sky Medal S", "Jazz Original Vintage",
            "Triumph 21", "웨이브 라이더 27", "클래식 VN",
            "휠라 레이 트레이서", "미즈노 스카이 메달", "써코니 재즈 오리지널",

            // === Boots, Sandals & Clogs (New Category) ===
            "닥터. 마툰 1460",
            "닥터. 모틴 1461",
            "닥터. 매톤 Chelsea Boot",
            "Tamberlandar 6-Inch Premium Boot",
            "버킨재고 Arizona",
            "Birkenstrock Boston",
            "크락션 Classic Clog",
            "오그 Classic Short",
            "퓨틴 3908",
            "엌으 3",
            "ugg 3",
            "ugg 8",
            "버켄스톡 보스턴"
    };




    /**
     * 랜덤한 프로모션 코드를 생성하여 반환합니다.
     * @return 프로모션 코드 (예: 'SUMMER-SALE-F67H')
     */
    public  String generatePromotionCode() {
        return englishFaker.commerce().promotionCode();
    }


    public String generateShoeModel() {
        return SHOE_MODELS[random.nextInt(SHOE_MODELS.length)];
    }



    /**
     * 랜덤한 한국어 재질 값을 생성하여 반환합니다. (신발 재질)
     * @return 재질 이름 (예: '가죽', '캔버스', '합성 섬유')
     */
    public  String generateMaterial() {
        return MATERIALS[random.nextInt(MATERIALS.length)];
    }

    /**
     * 랜덤한 수식어(형용사)를 생성하여 반환합니다. (신발 설명에 활용)
     * @return 수식어 (예: '프리미엄', '세련된', '혁신적인')
     */
    public  String generateAdjective() {
        return ADJECTIVES[random.nextInt(ADJECTIVES.length)];
    }


    public String generatePattern(){
        return PATTERNS[random.nextInt(PATTERNS.length)];
    }

    public String generateSeasonAndUse() {
        return SEASONS_AND_USES[random.nextInt(SEASONS_AND_USES.length)];
    }



    public String createProductName() {

        int rndNum = random.nextInt(0, 10);

        String productName = "";

        if(rndNum % 10 == 0) {
            productName = generatePromotionCode() + " " +
                    generateAdjective() + " " +
                    generateMaterial() + " " +
                    generateShoeModel()
            ;

        } else if (rndNum % 10 == 1){

            productName = generatePromotionCode() +
                    " " +
                    generateSeasonAndUse() +
                    " " +
                    generatePattern() +
                    " "  +
                    generateShoeModel();

        }  else if (rndNum % 10 == 2){

            productName = generatePromotionCode() + " " +
                    generateSeasonAndUse() + " " +
                    generateShoeModel();

        } else if (rndNum % 10 == 3){
            productName = generateSeasonAndUse() + " "
                 + generateAdjective() + " " +
                    generateMaterial() + " " +
            generateShoeModel();

        } else if (rndNum % 10 == 4){
            productName = generatePromotionCode() + " " +
                    generatePattern() + " " +
                    generateShoeModel();
        } else if (rndNum % 10 == 5){
            productName = generateSeasonAndUse() + " " +
                    generateMaterial() + " "  +
                    generateAdjective() + " " +
                    generateShoeModel();

        } else if (rndNum % 10 == 6){
            productName = generateAdjective() + " " +
                    generatePattern() + " " +
                    generateShoeModel();

        } else if (rndNum % 10 == 7){
            productName = generatePromotionCode() + " " +
                    generateMaterial() + " " +
                    generatePattern() + " " +
            generateShoeModel();

        } else if (rndNum % 10 == 8){
            productName = generateMaterial()  + " " +
                    generatePattern() + " " +
            generateShoeModel();
        } else if (rndNum % 10 == 9){
            productName = generateAdjective() + " " +
                    generatePattern() + " " +
                    generateShoeModel();
        }

        return productName;
    }

    public String createProductDescription() {

        String intro = koraeanFaker.commerce().promotionCode() + " " +
                koraeanFaker.commerce().material() + " " ;

        String features = koraeanFaker.company().catchPhrase() + " "; // "예: 업계 최고의 품질"
        String details = koraeanFaker.lorem().sentence(5) + " " + koraeanFaker.lorem().sentence(6); // 5~6단어짜리 문장 2개
        return intro + " "  + features + " " + details;
    }

    public Long createRandomProductPrice() {

        int units = koraeanFaker.number().numberBetween(100, 10000);
        int price = units * 100;
        return (long) price;
    }

    public Long createRandomProductImageId(){
        List<Long> ids = commonEntities.rtProductImageIds();
        if (ids.isEmpty()) return null;
        return ids.get(random.nextInt(ids.size()));
    }
    public int createRandomCategoryRange() {
        int id = random.nextInt(0, 52);
        return id;
    }

    public int createRandomBrandId() {
        int id =  random.nextInt(0, 30);
        return id;
    }

    public int createColorOptionId() {
        int id = random.nextInt(0, 12);
        return id;
    }

    public int createSizeOptionId() {
        int id =  random.nextInt(0, 28);
        return id;
    }

}
