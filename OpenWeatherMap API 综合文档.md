# OpenWeatherMap API 综合文档

## 目录

1.  [引言](#引言)
2.  [通用信息](#通用信息)
    *   [API密钥 (appid)](#api密钥-appid)
    *   [地理定位](#地理定位)
    *   [响应格式 (mode)](#响应格式-mode)
    *   [计量单位 (units)](#计量单位-units)
    *   [多语言支持 (lang)](#多语言支持-lang)
    *   [时间格式](#时间格式)
3.  [API 详细文档](#api-详细文档)
    *   [当前天气数据 API](#当前天气数据-api)
    *   [小时级天气预报 API (4天)](#小时级天气预报-api-4天)
    *   [5天天气预报 API (3小时间隔)](#5天天气预报-api-3小时间隔)
    *   [16天天气预报 API](#16天天气预报-api)
    *   [30天气候预报 API](#30天气候预报-api)
    *   [历史天气数据 API](#历史天气数据-api)
    *   [统计天气数据 API](#统计天气数据-api)

---

## 1. 引言

本文档整合了多个OpenWeatherMap API产品的信息，旨在为开发者提供一个全面、统一的参考指南。涵盖了获取当前天气、天气预报（小时级、5天、16天、30天气候预报）、历史天气数据、统计天气数据等功能。

---

## 2. 通用信息

以下信息适用于本文档中描述的大部分或全部API。

### API密钥 (appid)

*   **必需参数**: 几乎所有的API调用都需要您的个人API密钥。
*   **获取**: 您可以在登录OpenWeatherMap账户后，在个人页面的"API key"标签下找到。
*   **用法**: 通常作为URL查询参数 `appid={API key}` 添加到请求中。

### 地理定位

*   **推荐方式**: 使用地理坐标（纬度 `lat` 和经度 `lon`）进行API调用是最准确、最推荐的方式。

### 响应格式 (mode)

*   **默认**: 大多数API默认返回 `JSON` 格式的数据。
*   **可选**: 部分API支持通过 `mode` 参数指定其他格式，如 `xml` 或 `html`。
    *   `mode=xml`: 获取XML格式响应。
    *   `mode=html`: 获取HTML格式响应（仅部分API支持）。

### 计量单位 (units)

可以通过 `units` 参数控制返回数据的单位。主要支持以下三种体系：

*   **`standard` (标准)**:
    *   温度: 开尔文 (K) - **默认值**
    *   风速: 米/秒 (m/s)
*   **`metric` (公制)**:
    *   温度: 摄氏度 (°C)
    *   风速: 米/秒 (m/s)
*   **`imperial` (英制)**:
    *   温度: 华氏度 (°F)
    *   风速: 英里/小时 (mph)

**示例**: `&units=metric`

### 多语言支持 (lang)

*   可以通过 `lang` 参数获取指定语言的API输出。
*   这会影响响应中的 `description` 字段（天气描述）和 `city.name` 字段（城市名称）。
*   **支持中文**:
    *   `lang=zh_cn`: 简体中文
    *   `lang=zh_tw`: 繁体中文
*   **支持语言列表**: (sq, af, ar, az, eu, be, bg, ca, zh_cn, zh_tw, hr, cz, da, nl, en, fi, fr, gl, de, el, he, hi, hu, is, id, it, ja, kr, ku, la, lt, mk, no, fa, pl, pt, pt_br, ro, ru, sr, sk, sl, sp/es, sv/se, th, tr, ua/uk, vi, zu)
*   **示例**: `&lang=zh_cn`

### 时间格式

*   API响应中的时间戳通常使用 **Unix时间戳 (UTC)** 格式。
*   部分API响应中可能包含易于阅读的文本格式时间 `dt_txt`，但也通常是UTC时间。
*   地图API的时间参数 `tm` 或 `date` 也需要使用 **Unix时间戳 (UTC)**。

---

## 3. API 详细文档

### 当前天气数据 API

*   **概述**: 获取全球任意位置的实时天气状况。数据来源包括模型、卫星、雷达和气象站。
*   **API调用**:
    ```
    https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}
    ```
*   **必需参数**: `lat`, `lon`, `appid`
*   **可选参数**: `mode` (json/xml/html), `units` (standard/metric/imperial), `lang`
*   **主要响应字段**:
    *   `coord`: 经纬度
    *   `weather`: 天气状况 (id, main, description, icon)
    *   `main`: 主要数据 (temp, feels_like, temp_min, temp_max, pressure, humidity, sea_level, grnd_level)
    *   `visibility`: 能见度 (米)
    *   `wind`: 风况 (speed, deg, gust)
    *   `clouds`: 云量 (all)
    *   `rain`: 降雨量 (rain.1h)
    *   `snow`: 降雪量 (snow.1h)
    *   `dt`: 数据时间 (Unix UTC)
    *   `sys`: 系统数据 (country, sunrise, sunset)
    *   `timezone`: 与UTC时差(秒)
    *   `id`: 城市ID
    *   `name`: 城市名称
    *   `cod`: 响应状态码
*   **注意事项**:
    *   `main.temp_min` 和 `main.temp_max` 在当前天气API中指当前时刻城市内不同观测点的温度范围，并非日最高/最低温。
    *   如果响应中缺少某些参数（如 `rain.1h`），表示该天气现象当前未发生。

返回类似
{
	"coord": {
		"lon": 110.09,
		"lat": 32.26
	},
	"weather": [
		{
			"id": 800,
			"main": "Clear",
			"description": "晴",
			"icon": "01n"
		}
	],
	"base": "stations",
	"main": {
		"temp": 25.63,
		"feels_like": 25.21,
		"temp_min": 25.63,
		"temp_max": 25.63,
		"pressure": 1003,
		"humidity": 37,
		"sea_level": 1003,
		"grnd_level": 942
	},
	"visibility": 10000,
	"wind": {
		"speed": 3.08,
		"deg": 21,
		"gust": 7.47
	},
	"clouds": {
		"all": 2
	},
	"dt": 1744976799,
	"sys": {
		"country": "CN",
		"sunrise": 1744927630,
		"sunset": 1744974628
	},
	"timezone": 28800,
	"id": 1783825,
	"name": "Zhushan Chengguanzhen",
	"cod": 200
}

### 小时级天气预报 API (4天)

*   **概述**: 提供未来4天（96小时）的逐小时天气预报。
*   **API调用**:
    ```
    https://pro.openweathermap.org/data/2.5/forecast/hourly?lat={lat}&lon={lon}&appid={API key}
    ```
    *(注意: URL中包含 `pro.`，可能需要特定订阅)*
*   **必需参数**: `lat`, `lon`, `appid`
*   **可选参数**: `mode` (json/xml), `cnt` (返回时间点数量), `lang`, `units`
*   **主要响应字段**:
    *   `cod`, `message`, `cnt`
    *   `list`: 包含多个小时预报对象的数组
        *   `dt`: 预报时间 (Unix UTC)
        *   `main`: (temp, feels_like, temp_min, temp_max, pressure,  sea_level, grnd_level, humidity, temp_kf)
        *   `weather`: (id, main, description, icon)
        *   `clouds`: (all)
        *   `wind`: (speed, deg, gust)
        *   `visibility`: 能见度 (米)
        *   `pop`: 降水概率 (0-1)
        *   `rain`: (rain.1h)
        *   `snow`: (snow.1h)
        *   `dt_txt`: 预报时间文本 (UTC)
    *   `city`: 城市信息 (id, name, coord, country, population, timezone, sunrise, sunset)
*   **注意事项**: `cnt` 参数可以限制返回的小时数量，最多96个。

返回类似
{
	"cod": "200",
	"message": 0,
	"cnt": 96,
	"list": [
		{
			"dt": 1744977600,
			"main": {
				"temp": 25.63,
				"feels_like": 25.21,
				"temp_min": 25.63,
				"temp_max": 25.63,
				"pressure": 1003,
				"sea_level": 1003,
				"grnd_level": 942,
				"humidity": 37,
				"temp_kf": 0
			},
			"weather": [
				{
					"id": 800,
					"main": "Clear",
					"description": "晴",
					"icon": "01n"
				}
			],
			"clouds": {
				"all": 2
			},
			"wind": {
				"speed": 3.08,
				"deg": 21,
				"gust": 7.47
			},
			"visibility": 10000,
			"pop": 0,
			"sys": {
				"pod": "n"
			},
			"dt_txt": "2025-04-18 12:00:00"
		},
		{
			"dt": 1744981200,
			"main": {
				"temp": 25.2,
				"feels_like": 24.77,
				"temp_min": 23.5,
				"temp_max": 25.2,
				"pressure": 1003,
				"sea_level": 1003,
				"grnd_level": 944,
				"humidity": 38,
				"temp_kf": 1.7
			},
			"weather": [
				{
					"id": 800,
					"main": "Clear",
					"description": "晴",
					"icon": "01n"
				}
			],
			"clouds": {
				"all": 8
			},
			"wind": {
				"speed": 3.06,
				"deg": 12,
				"gust": 7.38
			},
			"visibility": 10000,
			"pop": 0.37,
			"sys": {
				"pod": "n"
			},
			"dt_txt": "2025-04-18 13:00:00"
		},
		{
			"dt": 1745319600,
			"main": {
				"temp": 16.86,
				"feels_like": 16.01,
				"temp_min": 16.86,
				"temp_max": 16.86,
				"pressure": 1013,
				"sea_level": 1013,
				"grnd_level": 951,
				"humidity": 54,
				"temp_kf": 0
			},
			"weather": [
				{
					"id": 804,
					"main": "Clouds",
					"description": "阴，多云",
					"icon": "04d"
				}
			],
			"clouds": {
				"all": 98
			},
			"wind": {
				"speed": 0.4,
				"deg": 173,
				"gust": 0.44
			},
			"visibility": 10000,
			"pop": 0,
			"sys": {
				"pod": "d"
			},
			"dt_txt": "2025-04-22 11:00:00"
		}
	],
	"city": {
		"id": 1783825,
		"name": "Zhushan Chengguanzhen",
		"coord": {
			"lat": 32.26,
			"lon": 110.09
		},
		"country": "CN",
		"population": 1000,
		"timezone": 28800,
		"sunrise": 1744927630,
		"sunset": 1744974628
	}
}

### 16天天气预报 API (3小时间隔)

*   **概述**: 提供最多未来16天的每日天气预报。
*   **API调用**:

    ```
    https://api.openweathermap.org/data/2.5/forecast/daily?lat={lat}&lon={lon}&cnt={cnt}&appid={API key}
    ```
*   **必需参数**: `lat`, `lon`, `appid`
*   **可选参数**: `cnt` (返回的时间戳数量), `mode` (json/xml), `units`, `lang`
*   **示例调用 (7天)**: `...&cnt=63...`
*   **主要响应字段**:
    *   `city`: 城市信息 (id, name, coord, country, population, timezone)
    *   `cod`, `message`, `cnt` (返回的时间戳数量)
    *   `list`: 包含多个每日预报对象的数组
        *   `dt`: 预报日期 (Unix UTC, 通常指当日中午)
        *   `sunrise`, `sunset`: 日出日落时间 (Unix UTC)
        *   `temp`: 温度对象 (day, min, max, night, eve, morn)
        *   `feels_like`: 体感温度对象 (day, night, eve, morn)
        *   `pressure`: 气压 (hPa)
        *   `humidity`: 湿度 (%)
        *   `weather`: (id, main, description, icon)
        *   `speed`: 风速
        *   `deg`: 风向 (度)
        *   `gust`: 阵风
        *   `clouds`: 云量 (%)
        *   `pop`: 降水概率 (0-1)
        *   `rain`: 当日降雨量 (mm)
        *   `snow`: 当日降雪量 (mm)
*   **注意事项**: `temp.min` 和 `temp.max` 在此API中指当天的最低和最高温度。

返回类似
{
	"cod": "200",
	"message": 0,
	"cnt": 3,
	"list": [
		{
			"dt": 1744988400,
			"main": {
				"temp": 23.95,
				"feels_like": 23.65,
				"temp_min": 21.17,
				"temp_max": 23.95,
				"pressure": 1005,
				"sea_level": 1005,
				"grnd_level": 947,
				"humidity": 48,
				"temp_kf": 2.78
			},
			"weather": [
				{
					"id": 500,
					"main": "Rain",
					"description": "小雨",
					"icon": "10n"
				}
			],
			"clouds": {
				"all": 11
			},
			"wind": {
				"speed": 0.22,
				"deg": 294,
				"gust": 0.62
			},
			"visibility": 10000,
			"pop": 0.5,
			"rain": {
				"3h": 0.44
			},
			"sys": {
				"pod": "n"
			},
			"dt_txt": "2025-04-18 15:00:00"
		},
		{
			"dt": 1744999200,
			"main": {
				"temp": 21.13,
				"feels_like": 20.81,
				"temp_min": 19.02,
				"temp_max": 21.13,
				"pressure": 1006,
				"sea_level": 1006,
				"grnd_level": 946,
				"humidity": 58,
				"temp_kf": 2.11
			},
			"weather": [
				{
					"id": 802,
					"main": "Clouds",
					"description": "多云",
					"icon": "03n"
				}
			],
			"clouds": {
				"all": 39
			},
			"wind": {
				"speed": 1.71,
				"deg": 101,
				"gust": 1.7
			},
			"visibility": 10000,
			"pop": 0.29,
			"sys": {
				"pod": "n"
			},
			"dt_txt": "2025-04-18 18:00:00"
		},
		{
			"dt": 1745010000,
			"main": {
				"temp": 17.67,
				"feels_like": 17.19,
				"temp_min": 17.67,
				"temp_max": 17.67,
				"pressure": 1008,
				"sea_level": 1008,
				"grnd_level": 946,
				"humidity": 65,
				"temp_kf": 0
			},
			"weather": [
				{
					"id": 804,
					"main": "Clouds",
					"description": "阴，多云",
					"icon": "04n"
				}
			],
			"clouds": {
				"all": 98
			},
			"wind": {
				"speed": 0.84,
				"deg": 114,
				"gust": 0.98
			},
			"visibility": 10000,
			"pop": 0,
			"sys": {
				"pod": "n"
			},
			"dt_txt": "2025-04-18 21:00:00"
		}
	],
	"city": {
		"id": 1783825,
		"name": "Zhushan Chengguanzhen",
		"coord": {
			"lat": 32.26,
			"lon": 110.09
		},
		"country": "CN",
		"population": 1000,
		"timezone": 28800,
		"sunrise": 1744927630,
		"sunset": 1744974628
	}
}

### 30天气候预报 API (每日12点整)

*   **概述**: 提供未来30天的 **每日** 天气预报数据。此预报被称为“气候预报”。数据支持JSON和XML格式。
*   **API调用**:
    ```
    https://pro.openweathermap.org/data/2.5/forecast/climate?lat={lat}&lon={lon}&appid={API key}
    ```
    *(注意: URL中包含 `pro.`，可能需要特定订阅)*
*   **必需参数**: `lat`, `lon`, `appid`
*   **可选参数**:
    *   `cnt`: 返回天数 (1-30)
    *   `mode`: 响应格式 (json/xml, 默认json)
    *   `units`: 计量单位 (standard/metric/imperial, 默认standard)
    *   `lang`: 语言代码
*   **主要响应字段**:
    *   `cod`: 响应状态码
    *   `message`: 内部参数
    *   `city`: 城市信息
        *   `id`: 城市ID
        *   `name`: 城市名称
        *   `coord`: 地理坐标 (`lat`, `lon`)
        *   `country`: 国家代码
        *   `population`: 城市人口
        *   `timezone`: 与UTC时差(秒)
    *   `list`: 包含多个每日预报对象的数组 (最多30个)
        *   `dt`: 预报日期 (Unix UTC)
        *   `sunrise`: 日出时间 (Unix UTC)
        *   `sunset`: 日落时间 (Unix UTC)
        *   `temp`: 温度对象 (`day`, `min`, `max`, `night`, `eve`, `morn`)
        *   `feels_like`: 体感温度对象 (`day`, `night`, `eve`, `morn`)
        *   `pressure`: 海平面气压 (hPa)
        *   `humidity`: 湿度 (%)
        *   `weather`: 天气状况数组 (`id`, `main`, `description`, `icon`)
        *   `speed`: 风速
        *   `deg`: 风向 (度, 气象角度)
        *   `clouds`: 云量 (%)
        *   `rain`: 降雨量 (mm) - 仅支持mm单位
        *   `snow`: 降雪量 (mm) - 仅支持mm单位
*   **注意事项**:
    *   URL包含`pro.`，通常表示需要付费订阅。
    *   内置地理编码功能(按城市名、ID、邮编查询)已弃用，建议使用独立Geocoding API。
    *   可以使用 `cnt` 参数限制返回的天数 (1-30)。
    *   降水 `rain` 和 `snow` 仅以 `mm` 为单位返回。
    *   支持JSONP回调函数 (`callback=functionName`)。

返回类似
{
	"city": {
		"id": 1783825,
		"name": "Zhushan Chengguanzhen",
		"coord": {
			"lon": 110.09,
			"lat": 32.26
		},
		"country": "CN",
		"population": 1000,
		"timezone": 28800
	},
	"code": "200",
	"message": 29.2366048,
	"cnt": 3,
	"list": [
		{
			"dt": 1744948800,
			"sunrise": 1744927630,
			"sunset": 1744974628,
			"temp": {
				"day": 31.83,
				"min": 20.6,
				"max": 31.83,
				"night": 20.63,
				"eve": 24.02,
				"morn": 21.79
			},
			"feels_like": {
				"day": 32.77,
				"night": 21.15,
				"eve": 24.57,
				"morn": 22.19
			},
			"pressure": 1003,
			"humidity": 44,
			"weather": [
				{
					"id": 500,
					"main": "Rain",
					"description": "小雨",
					"icon": "10d"
				}
			],
			"speed": 3.18,
			"deg": 6,
			"clouds": 15,
			"rain": 7.18
		},
		{
			"dt": 1745035200,
			"sunrise": 1745013962,
			"sunset": 1745061070,
			"temp": {
				"day": 26.54,
				"min": 19.63,
				"max": 26.54,
				"night": 20.1,
				"eve": 21.83,
				"morn": 19.63
			},
			"feels_like": {
				"day": 26.54,
				"night": 20.65,
				"eve": 22.31,
				"morn": 19.92
			},
			"pressure": 1009,
			"humidity": 58,
			"weather": [
				{
					"id": 500,
					"main": "Rain",
					"description": "小雨",
					"icon": "10d"
				}
			],
			"speed": 3.33,
			"deg": 93,
			"clouds": 52,
			"rain": 3.91
		},
		{
			"dt": 1745121600,
			"sunrise": 1745100294,
			"sunset": 1745147513,
			"temp": {
				"day": 27.84,
				"min": 20.1,
				"max": 27.84,
				"night": 20.57,
				"eve": 23.5,
				"morn": 20.57
			},
			"feels_like": {
				"day": 29.22,
				"night": 21.16,
				"eve": 23.97,
				"morn": 20.98
			},
			"pressure": 1007,
			"humidity": 60,
			"weather": [
				{
					"id": 500,
					"main": "Rain",
					"description": "小雨",
					"icon": "10d"
				}
			],
			"speed": 4.63,
			"deg": 142,
			"clouds": 40,
			"rain": 0.1
		}
	]
}

### 历史天气数据 API

*   **概述**: 获取指定位置、指定时间段内的 **每小时** 历史天气数据。可以获取过去 1 年内的天气数据。
*   **API调用**:
    *   指定起止时间:
        ```
        https://history.openweathermap.org/data/2.5/history/city?lat={lat}&lon={lon}&type=hour&start={start}&end={end}&appid={API key}
        ```
    *   指定开始时间和数量:
        ```
        https://history.openweathermap.org/data/2.5/history/city?lat={lat}&lon={lon}&type=hour&start={start}&cnt={cnt}&appid={API key}
        ```
*   **必需参数**: `lat`, `lon`, `type=hour`, `appid`
*   **可选参数**: `start` (Unix UTC), `end` (Unix UTC), `cnt` (小时数), `units`
*   **主要响应字段**:
    *   `message`, `cod`, `city_id`, `calctime`, `cnt`
    *   `list`: 包含多个小时历史数据的数组
        *   `dt`: 时间戳 (Unix UTC)
        *   `main`: (temp, feels_like, pressure, humidity, temp_min, temp_max, sea_level, grnd_level)
        *   `wind`: (speed, deg)
        *   `clouds`: (all)
        *   `rain`: (rain.1h, rain.3h) - 过去1或3小时降雨量
        *   `snow`: (snow.1h, snow.3h) - 过去1或3小时降雪量
        *   `weather`: (id, main, description, icon) - *虽然文档示例没列出，但通常应包含*
*   **注意事项**:
    *   `type` 参数 **必须** 设置为 `hour`。
    *   `start` 和 `end` 为Unix时间戳 (UTC)。
    *   单次API请求最多获取一周的数据。如需更长时间范围，需多次调用。
    *   如果响应中缺少参数（如`rain`），表示该时段无此现象。

返回类似
{
	"message": "Count: 145",
	"cod": "200",
	"city_id": 1,
	"calctime": 0.2374172,
	"cnt": 145,
	"list": [
		{
			"dt": 1717171200,
			"main": {
				"temp": 22.87,
				"feels_like": 22.73,
				"pressure": 1013,
				"humidity": 58,
				"temp_min": 22.87,
				"temp_max": 22.87
			},
			"wind": {
				"speed": 1.11,
				"deg": 111,
				"gust": 1.16
			},
			"clouds": {
				"all": 100
			},
			"weather": [
				{
					"id": 804,
					"main": "Clouds",
					"description": "overcast clouds",
					"icon": "04n"
				}
			]
		},
		{
			"dt": 1717174800,
			"main": {
				"temp": 22.34,
				"feels_like": 22.17,
				"pressure": 1013,
				"humidity": 59,
				"temp_min": 22.34,
				"temp_max": 22.34
			},
			"wind": {
				"speed": 0.68,
				"deg": 129,
				"gust": 0.84
			},
			"clouds": {
				"all": 100
			},
			"weather": [
				{
					"id": 804,
					"main": "Clouds",
					"description": "overcast clouds",
					"icon": "04n"
				}
			]
		},
		{
			"dt": 1717689600,
			"main": {
				"temp": 21.12,
				"feels_like": 21.25,
				"pressure": 1011,
				"humidity": 75,
				"temp_min": 21.12,
				"temp_max": 21.12
			},
			"wind": {
				"speed": 1.11,
				"deg": 32,
				"gust": 1.54
			},
			"clouds": {
				"all": 95
			},
			"weather": [
				{
					"id": 804,
					"main": "Clouds",
					"description": "overcast clouds",
					"icon": "04n"
				}
			]
		}
	]
}

### 统计天气数据 API

*   **概述**: 获取基于历史天气数据计算出的 **统计** 指标（平均值、中位数、最大/最小值等），可按月、日聚合。**注意：这不是原始历史数据。**
*   **API调用**: (基础URL: `https://history.openweathermap.org/data/2.5/aggregated/`)
    *   **月度聚合**: `month?month={month}&lat={lat}&lon={lon}&appid={API key}`
    *   **每日聚合**: `day?month={month}&day={day}&lat={lat}&lon={lon}&appid={API key}`
*   **必需参数**: `lat`, `lon`, `appid`。月度和每日聚合还需 `month` (1-12)，每日聚合还需 `day` (1-31)。
*   **响应格式**: JSON only.
*   **主要响应字段**:
    *   `cod`, `city_id`, `calctime`
    *   `result`: 包含各参数统计数据的对象
        *   `temp`: 温度统计 (record_min, record_max, average_min, average_max, median, mean, p25, p75, st_dev, num) 单位: K
        *   `pressure`: 气压统计 (min, max, median, mean, p25, p75, st_dev, num) 单位: hPa
        *   `humidity`: 湿度统计 (min, max, median, mean, p25, p75, st_dev, num) 单位: %
        *   `wind`: 风速统计 (min, max, median, mean, p25, p75, st_dev, num) 单位: m/s
        *   `precipitation`: 降水量统计 (min, max, median, mean, p25, p75, st_dev, num) 单位: mm
        *   `clouds`: 云量统计 (min, max, median, mean, p25, p75, st_dev, num) 单位: %
*   **注意事项**: 数据每小时更新一次。此API用于气候分析，而非获取特定日期的实际天气。

返回类似
{
	"cod": 200,
	"city_id": 1794904,
	"calctime": 0.144147176,
	"result": {
		"month": 5,
		"day": 1,
		"temp": {
			"record_min": 282.4,
			"record_max": 306.73,
			"average_min": 286.35,
			"average_max": 298.48,
			"median": 291.32,
			"mean": 291.89,
			"p25": 288.04,
			"p75": 295.37,
			"st_dev": 5.08,
			"num": 288
		},
		"pressure": {
			"min": 941,
			"max": 1025,
			"median": 1007,
			"mean": 990.37,
			"p25": 960,
			"p75": 1015,
			"st_dev": 28.24,
			"num": 288
		},
		"humidity": {
			"min": 14,
			"max": 100,
			"median": 57,
			"mean": 59.09,
			"p25": 43,
			"p75": 76,
			"st_dev": 22.22,
			"num": 288
		},
		"wind": {
			"min": 0.22,
			"max": 5.43,
			"median": 1.57,
			"mean": 1.8,
			"p25": 0.96,
			"p75": 2.42,
			"st_dev": 1.04,
			"num": 288
		},
		"precipitation": {
			"min": 0,
			"max": 3,
			"median": 0,
			"mean": 0.09,
			"p25": 0,
			"p75": 0,
			"st_dev": 0.31,
			"num": 288
		},
		"clouds": {
			"min": 0,
			"max": 100,
			"median": 38,
			"mean": 45.26,
			"p25": 8,
			"p75": 80,
			"st_dev": 37.25,
			"num": 288
		}
	}
}
